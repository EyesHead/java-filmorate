package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.entity.Review;
import ru.yandex.practicum.filmorate.repository.mapper.ReviewRowMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbReviewStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper reviewRowMapper;

    @Override
    public Review saveReview(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("reviews")
                .usingGeneratedKeyColumns("id");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_id", review.getUserId());
        parameters.put("film_id", review.getFilmId());
        parameters.put("is_positive", review.getIsPositive());
        parameters.put("content", review.getContent());
        long newReviewId = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return review.toBuilder().reviewId(newReviewId).build();
    }

    @Override
    public boolean updateReview(Review review) {
        final String UPDATE_REVIEW_QUERY = """
                UPDATE reviews SET is_positive = ?, content = ?
                WHERE id = ?;
                """;
        return jdbcTemplate.update(UPDATE_REVIEW_QUERY, review.getIsPositive(), review.getContent(), review.getReviewId()) != 0;
    }

    @Override
    public boolean removeReview(long reviewId) {
        final String REMOVE_REVIEW_QUERY = """
                DELETE FROM reviews WHERE id = ?;
                """;
        return jdbcTemplate.update(REMOVE_REVIEW_QUERY, reviewId) != 0;
    }

    @Override
    public Optional<Review> getReviewById(long reviewId) {
        final String GET_REVIEW_BY_ID_QUERY = """
                SELECT *
                FROM (SELECT * FROM reviews
                      WHERE id = ?) AS reviews
                LEFT JOIN (SELECT review_id, 2 * SUM(liked) - COUNT(user_id) AS useful
                           FROM reviews_likes
                           GROUP BY review_id) AS review_use
                ON reviews.id = review_use.review_id;
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(GET_REVIEW_BY_ID_QUERY, reviewRowMapper, reviewId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getAllReviews(int limit) {
        final String GET_ALL_REVIEWS = """
                SELECT *
                FROM reviews
                LEFT JOIN (SELECT review_id, (2 * SUM(liked)) - COUNT(user_id) AS useful FROM reviews_likes
                GROUP BY review_id) AS review_use
                ON review_use.review_id = reviews.id
                LIMIT ?;
                """;
        return jdbcTemplate.query(GET_ALL_REVIEWS, reviewRowMapper, limit);
    }

    @Override
    public List<Review> getReviewsByFilmId(long filmId, int amount) {
        final String GET_REVIEWS_BY_FILM_ID = """
                SELECT *
                FROM (SELECT * FROM reviews WHERE film_id = ?) as film_reviews
                LEFT JOIN (SELECT review_id, (2 * SUM(liked)) - COUNT(user_id) AS useful
                    FROM reviews_likes
                    GROUP BY review_id) as review_use
                ON film_reviews.id = review_use.review_id
                ORDER BY review_use.useful DESC
                LIMIT ?;
                """;
        return jdbcTemplate.query(GET_REVIEWS_BY_FILM_ID, reviewRowMapper, filmId, amount);
    }

    @Override
    public boolean addLikeDislike(long reviewId, long userId, int likeStatus) {
        final String ADD_REVIEW_LIKE_QUERY = """
                INSERT INTO reviews_likes (review_id, user_id, liked)
                VALUES (?, ?, ?);
                """;
        final String UPDATE_REVIEW_LIKE_QUERY = """
                UPDATE reviews_likes SET liked = ?
                WHERE review_id = ? AND user_id = ?
                """;
        try {
            return jdbcTemplate.update(ADD_REVIEW_LIKE_QUERY, reviewId, userId, likeStatus) != 0;
        } catch (DataIntegrityViolationException e) {
            return jdbcTemplate.update(UPDATE_REVIEW_LIKE_QUERY, likeStatus, reviewId, userId) != 0;
        }
    }

    @Override
    public boolean removeLikeDislike(long reviewId, long userId) {
        final String REMOVE_REVIEW_LIKE_QUERY = """
                DELETE FROM reviews_likes
                WHERE review_id = ? AND user_id = ?;
                """;
        return jdbcTemplate.update(REMOVE_REVIEW_LIKE_QUERY, reviewId, userId) != 0;
    }
}
