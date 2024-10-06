package ru.yandex.practicum.filmorate.service.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewValidator {
    private final ReviewStorage reviewStorage;

    public void checkReviewOnExist(long reviewId) throws NotFoundException {
        log.debug("(Validator) Начало проверки наличия отзыва с ID {}.", reviewId);

        reviewStorage.getReviewById(reviewId).orElseThrow(
                () -> new NotFoundException("Отзыв не найден. ID = " + reviewId)
        );

        log.debug("(Validator) Отзыв с ID {} найден. Проверка завершена.", reviewId);
    }
}
