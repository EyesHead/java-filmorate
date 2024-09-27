package ru.yandex.practicum.filmorate.service.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.DbReviewStorage;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewValidator {
    private final DbReviewStorage reviewStorage;

    public void checkReviewOnExistence(long reviewId) throws NotFoundException {
        log.info("Проверка наличия отзыва с ID {}.", reviewId);
        if (!reviewStorage.checkReview(reviewId))
            throw new NotFoundException("Отзыв с ID " + reviewId + " отсутствует");
        log.info("Отзыв с ID {} найден.", reviewId);
    }
}
