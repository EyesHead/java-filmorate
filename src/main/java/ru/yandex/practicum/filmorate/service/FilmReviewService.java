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
        log.info("(NEW) Добавление нового отзыва к фильму с id {}", review.getFilmId());
        Review savedReview = reviewStorage.saveReview(review);
        eventLogger.logEvent(savedReview.getUserId(), REVIEW, ADD, savedReview.getReviewId());
        log.info("Добавлен отзыв к фильму {} c id {}", savedReview.getFilmId(), savedReview.getReviewId());
        return savedReview;
    }

    public Review updateReview(Review review) {
        reviewValidator.checkReviewOnExist(review);
        log.info("(NEW) Обновление отзыва с id {}", review.getReviewId());
        reviewStorage.updateReview(review);
        Review updatedReview = reviewStorage.getReviewById(review.getReviewId()).get();
        long userId = updatedReview.getUserId();
        long entityId = updatedReview.getReviewId();
        eventLogger.logEvent(userId, REVIEW, UPDATE, entityId);
        log.info("Отзыв с id {} обновлен", entityId);
        return updatedReview;
    }

    public void deleteReview(long id) {
        log.info("(NEW) Удаление отзыва с id {}", id);
        Review reviewForDelete = getReview(id);
        long userId = reviewForDelete.getUserId();
        eventLogger.logEvent(userId, REVIEW, REMOVE, id);
        reviewStorage.removeReview(id);
        log.info("Отзыв с id {} удален", id);
    }

    public Review getReview(long id) {
        log.info("(NEW) Получение отзыва с id {}", id);
        return reviewStorage.getReviewById(id).orElseThrow(
                () -> new NotFoundException("Отзыв с id = " + id + " отсутствует")
        );
    }

    public List<Review> getFilmReviews(Optional<String> filmIdString, int amount) {
        log.info("(NEW) Получение отзывов к фильмам");
        if (filmIdString.isPresent()) {
            try {
                int filmId = Integer.parseInt(filmIdString.get());
                log.debug("Получение отзывов к фильму с id {}", filmId);
                return reviewStorage.getReviewsByFilmId(filmId, amount);
            } catch (ParseException e) {
                throw new InvalidDataRequestException("Не удалось получить id фильма");
            }
        } else {
            log.debug("Получение отзывов ко всем фильмам в количестве {}", amount);
            return reviewStorage.getAllReviews(amount).stream().sorted(comparing(Review::getUseful).reversed()).toList();
        }
    }

    public void addReviewLikeDislike(long reviewId, long userId, int likeStatus) {
        log.info("(NEW) Пользователь с id {} ставит оценку {} отзыву с id {}", userId, likeStatus, reviewId);
        reviewStorage.addLikeDislike(reviewId, userId, likeStatus);
    }

    public void deleteReviewLikeDislike(long reviewId, long userId) {
        log.info("(NEW) Пользователь с id {} удаляет оценку отзыва с id {}", userId, reviewId);
        reviewStorage.removeLikeDislike(reviewId, userId);
    }
}
