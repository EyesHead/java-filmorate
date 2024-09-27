package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ParseException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Review;
import ru.yandex.practicum.filmorate.exception.InvalidDataRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.DbReviewStorage;
import ru.yandex.practicum.filmorate.service.util.FilmValidator;
import ru.yandex.practicum.filmorate.service.util.ReviewValidator;
import ru.yandex.practicum.filmorate.service.util.UserValidator;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmReviewService {
    private final DbReviewStorage reviewStorage;
    private final UserValidator userValidator;
    private final FilmValidator filmValidator;
    private final ReviewValidator reviewValidator;

    public Review addNewReview(Review review) {
        userValidator.checkUserOnExist(review.getUserId());
        filmValidator.checkFilmOnExist(review.getFilmId());
        log.info("Добавление нового отзыва к фильму с id {}", review.getFilmId());
        return reviewStorage.addReview(review);
    }

    public void updateReview(Review review) {
        reviewValidator.checkReviewOnExistence(review.getReviewId());
        reviewStorage.updateReview(review);
    }

    public void deleteReview(long id) {
        log.info("Удаление отзыва с ID {}", id);
        if (!reviewStorage.removeReview(id)) throw new NotFoundException("Отзыв с id " + id + " отсутствует");
    }

    public Review getReview(long id) {
        return reviewStorage.getReviewById(id).orElseThrow(
                () -> new NotFoundException("Отзыв с id + " + id + " отсутствует")
        );
    }

    public List<Review> getFilmReviews(Optional<String> filmIdString, int amount) {
        if (filmIdString.isPresent()) {
            try {
                int filmId = Integer.parseInt(filmIdString.get());
                return reviewStorage.getReviewsByFilmId(filmId, amount);
            } catch (ParseException e) {
                throw new InvalidDataRequestException("Не удалось получить id фильма");
            }
        } else {
            return reviewStorage.getAllReviews(amount);
        }
    }

    public void addReviewLikeDislike(long reviewId, long userId, int likeStatus) {
        reviewStorage.addLikeDislike(reviewId, userId, likeStatus);
    }

    public void deleteReviewLikeDislike(long reviewId, long userId) {
        if (!reviewStorage.removeLikeDislike(reviewId, userId))
            throw new NotFoundException("Не удалось удалить оценку отзыва");
    }
}
