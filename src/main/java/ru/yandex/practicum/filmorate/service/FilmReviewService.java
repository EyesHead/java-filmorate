package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Review;
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
import static ru.yandex.practicum.filmorate.entity.EventType.REVIEW;

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
        log.info("(NEW) Пользователь с id {} добавляет новый отзыв к фильму с id {}", review.getUserId(), review.getFilmId());
        userValidator.checkUserOnExist(review.getUserId());
        filmValidator.checkFilmOnExist(review.getFilmId());

        Review savedReview = reviewStorage.saveReview(review);
        eventLogger.logEvent(savedReview.getUserId(), REVIEW, ADD, savedReview.getReviewId());

        log.info("(END) Добавлен новый отзыв с id {} к фильму с id {}", savedReview.getReviewId(), savedReview.getFilmId());
        return savedReview;
    }

    public Review updateReview(Review review) {
        log.info("(NEW) Обновление отзыва с id {}", review.getReviewId());
        reviewValidator.checkReviewOnExist(review.getReviewId());

        reviewStorage.updateReview(review);
        // Получение id пользователя, оставившего отзыв к фильму для записи в ленту событий
        Review updatedReview = reviewStorage.getReviewById(review.getReviewId()).get();
        long userId = updatedReview.getUserId();
        long entityId = updatedReview.getReviewId();
        eventLogger.logEvent(userId, REVIEW, UPDATE, entityId);

        log.info("(END) Отзыв с id {} обновлен", entityId);
        return updatedReview;
    }

    public void deleteReview(long reviewId) {
        log.info("(NEW) Удаление отзыва с reviewId {}", reviewId);
        // Получение id пользователя, оставившего отзыв к фильму для записи в ленту событий
        Review reviewForDelete = getReview(reviewId);
        long userId = reviewForDelete.getUserId();
        eventLogger.logEvent(userId, REVIEW, REMOVE, reviewId);

        if (reviewStorage.removeReview(reviewId)) {
            log.info("(END) Отзыв с reviewId {} удален", reviewId);
        } else {
            log.warn("(END) Отзыв с reviewId {} не удален, так как его не существует", reviewId);
        }
    }

    public Review getReview(long reviewId) {
        log.info("(NEW) Получение отзыва с reviewId {}", reviewId);
        return reviewStorage.getReviewById(reviewId).orElseThrow(
                () -> new NotFoundException("(END) Отзыв не найден. reviewId = " + reviewId));
    }

    public List<Review> getFilmReviews(Optional<Long> filmId, int limit) {
        if (filmId.isPresent()) {
            log.info("(NEW) Получение отзывов к фильму с id {} в количестве {}", filmId.get(), limit);
            return reviewStorage.getReviewsByFilmId(filmId.get(), limit);
        } else {
            log.info("(NEW) Получение отзывов ко всем фильмам в количестве {}", limit);
            return reviewStorage.getAllReviews(limit).stream()
                    .sorted(comparing(Review::getUseful).reversed())
                    .toList();
        }
    }

    public void addReviewLikeDislike(long reviewId, long userId, int likeStatus) {
        log.info("(NEW) Пользователь с id {} ставит оценку {} отзыву с id {}", userId, likeStatus, reviewId);

        userValidator.checkUserOnExist(userId);
        reviewValidator.checkReviewOnExist(reviewId);

        if (reviewStorage.addLikeDislike(reviewId, userId, likeStatus)) {
            log.info("(END) Оценка {} была добавлена отзыву с id {} пользователем с id {}", likeStatus, reviewId, userId);
        } else {
            log.warn("(END) Оценка {} не была добавлена отзыву с id {} пользователем с id {}", likeStatus, reviewId, userId);
        }
    }

    public void deleteReviewLikeDislike(long reviewId, long userId) {
        log.info("(NEW) Пользователь с id {} удаляет оценку отзыва с id {}", userId, reviewId);

        userValidator.checkUserOnExist(userId);
        reviewValidator.checkReviewOnExist(reviewId);

        if (reviewStorage.removeLikeDislike(reviewId, userId)) {
            log.info("(END) Пользователь с id {} удалил оценку отзыва с id {}", userId, reviewId);
        } else {
            log.warn("(END) Пользователь с id {} не ставил оценку отзыву с id = {}", userId, reviewId);
        }
    }
}
