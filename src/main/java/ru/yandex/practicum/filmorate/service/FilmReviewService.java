package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ParseException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Review;
import ru.yandex.practicum.filmorate.exception.InvalidDataRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.EventLogger;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;
import ru.yandex.practicum.filmorate.service.validators.FilmValidator;
import ru.yandex.practicum.filmorate.service.validators.ReviewValidator;
import ru.yandex.practicum.filmorate.service.validators.UserValidator;

import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static ru.yandex.practicum.filmorate.entity.EventOperation.*;
import static ru.yandex.practicum.filmorate.entity.EventType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmReviewService {
    private final ReviewStorage reviewStorage;
    private final EventLogger eventLogger;

    private final UserValidator userValidator;
    private final FilmValidator filmValidator;
    private final ReviewValidator reviewValidator;

    public Review addNewReview(Review review) {
        userValidator.checkUserOnExist(review.getUserId());
        filmValidator.checkFilmOnExist(review.getFilmId());
        log.info("Добавление нового отзыва к фильму с id {}", review.getFilmId());
        Review savedReview = reviewStorage.addReview(review);
        eventLogger.logEvent(savedReview.getUserId(), REVIEW, ADD, savedReview.getReviewId());
        return savedReview;
    }

    public Review updateReview(Review review) {
        reviewValidator.checkReviewOnExistence(review.getReviewId());
        reviewStorage.updateReview(review);
        Review updatedReview = reviewStorage.getReviewById(review.getReviewId()).get();
        long userId = updatedReview.getUserId();
        long entityId = updatedReview.getReviewId();
        eventLogger.logEvent(userId, REVIEW, UPDATE, entityId);
        return updatedReview;
    }

    public void deleteReview(long id) {
        log.info("Удаление отзыва с ID {}", id);
        Review reviewForDelete = getReview(id);
        long userId = reviewForDelete.getUserId();
        eventLogger.logEvent(userId, REVIEW, REMOVE, id);
        reviewStorage.removeReview(id);
    }

    public Review getReview(long id) {
        return reviewStorage.getReviewById(id).orElseThrow(
                () -> new NotFoundException("Отзыв с id = " + id + " отсутствует")
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
            return reviewStorage.getAllReviews(amount).stream().sorted(comparing(Review::getUseful).reversed()).toList();
        }
    }

    public void addReviewLikeDislike(long reviewId, long userId, int likeStatus) {
        reviewStorage.addLikeDislike(reviewId, userId, likeStatus);
        eventLogger.logEvent(userId, REVIEW, UPDATE, reviewId);
    }

    public void deleteReviewLikeDislike(long reviewId, long userId) {
        reviewStorage.removeLikeDislike(reviewId, userId);
        eventLogger.logEvent(userId, REVIEW, UPDATE, reviewId);
    }
}
