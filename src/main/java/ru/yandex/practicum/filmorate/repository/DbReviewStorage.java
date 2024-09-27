package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.entity.Review;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.mapper.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper reviewRowMapper;
    private static final String ADD_REVIEW_QUERY = """
            INSERT INTO reviews (user_id, film_id, is_positive, content)
            VALUES (?, ?, ?, ?);
            """;
    private static final String UPDATE_REVIEW_QUERY = """
            UPDATE reviews SET is_positive = ?, content = ?
            WHERE user_id = ? AND film_id = ?;
            """;
    private static final String REMOVE_REVIEW_QUERY = """
            DELETE FROM reviews WHERE id = ?;
            """;
    private static final String GET_REVIEW_BY_ID_QUERY = """
            SELECT *
            FROM (SELECT review_id, (2 * SUM(liked)) - COUNT(user_id) AS useful FROM reviews_likes
            WHERE review_id = ?
            GROUP BY review_id) AS review_use
            RIGHT JOIN reviews ON review_use.review_id = reviews.id;
            """;
    private static final String GET_ALL_REVIEWS = """
            SELECT *
            FROM (SELECT review_id, (2 * SUM(liked)) - COUNT(user_id) AS useful FROM review_likes
            WHERE review_id = ?
            GROUP BY review_id) AS review_use
            RIGHT JOIN reviews ON review_use.review_id = reviews.review_id
            ORDER BY review_use.useful DESC
            LIMIT ?;
            """;
    private static final String GET_REVIEWS_BY_FILM_ID = """
            SELECT *
            FROM (SELECT * FROM reviews WHERE film_id = ?) as film_reviews
            LEFT JOIN (SELECT review_id, (2 * SUM(liked)) - COUNT(user_id) AS useful
                FROM reviews_likes
                GROUP BY review_id) as review_use
            ON film_reviews.id = review_use.review_id
            ORDER BY review_use.useful DESC
            LIMIT ?;
            """;

    private static final String CHECK_REVIEW_QUERY = """
            SELECT COUNT(id)
            FROM reviews
            WHERE id = ?;
            """;
    private static final String ADD_REVIEW_LIKE_QUERY = """
            INSERT INTO reviews_likes (review_id, user_id, liked)
            VALUES (?, ?, ?);
            """;
    private static final String REMOVE_REVIEW_LIKE_QUERY = """
            DELETE FROM reviews_likes
            WHERE review_id = ? AND user_id = ?;
            """;

    private static final String UPDATE_REVIEW_LIKE_QUERY = """
            UPDATE reviews_likes SET liked = ?
            WHERE review_id = ? AND user_id = ?
            """;

    public Review addReview(Review review) {
        long newReviewId = insert(ADD_REVIEW_QUERY, review.getUserId(), review.getFilmId(), review.getIsPositive(), review.getContent());
        review.setReviewId(newReviewId);
        log.info("Добавлен отзыв к фильму {} c id {}", review.getFilmId(), review.getReviewId());
        return review;
    }

    public void updateReview(Review review) {
        jdbcTemplate.update(UPDATE_REVIEW_QUERY, review.getIsPositive(), review.getContent(), review.getUserId(), review.getFilmId());
    }

    public boolean removeReview(long reviewId) {
        return jdbcTemplate.update(REMOVE_REVIEW_QUERY, reviewId) > 0;
    }

    public boolean checkReview(long reviewId) {
        return jdbcTemplate.queryForObject(CHECK_REVIEW_QUERY, Integer.class, reviewId) != 0;
    }

    public Optional<Review> getReviewById(long reviewId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(GET_REVIEW_BY_ID_QUERY, reviewRowMapper, reviewId));
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NotFoundException("Отзыв с ID " + reviewId + " отсутствует");
        }
    }

    public List<Review> getAllReviews(int amount) {
        return jdbcTemplate.query(GET_ALL_REVIEWS, reviewRowMapper, amount);
    }

    public List<Review> getReviewsByFilmId(long filmId, int amount) {
        return jdbcTemplate.query(GET_REVIEWS_BY_FILM_ID, reviewRowMapper, filmId, amount);
    }

    public void addLikeDislike(long reviewId, long userId, int likeStatus) {
        try {
            jdbcTemplate.update(ADD_REVIEW_LIKE_QUERY, reviewId, userId, likeStatus);
        } catch (DataIntegrityViolationException e) {
            jdbcTemplate.update(UPDATE_REVIEW_LIKE_QUERY, likeStatus, reviewId, userId);
        }
    }

    public boolean removeLikeDislike(long reviewId, long userId) {
        return jdbcTemplate.update(REMOVE_REVIEW_LIKE_QUERY, reviewId, userId) > 0;
    }

    protected Long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        if (id != 0) {
            return id;
        } else {
            throw new RuntimeException("Не удалось сохранить данные");
        }
    }
}
