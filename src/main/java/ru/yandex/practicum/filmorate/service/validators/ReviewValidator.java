package ru.yandex.practicum.filmorate.service.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.entity.Review;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewValidator {
    private final ReviewStorage reviewStorage;

    public void validateReview(Review review) throws NotFoundException {
        log.info("Проверка наличия отзыва с ID {}.", review.getReviewId());
        reviewStorage.getReviewById(review.getReviewId()).orElseThrow(
                () -> new NotFoundException("Отзыв с ID " + review.getReviewId() + " отсутствует")
        );
        log.info("Отзыв с ID {} найден.", review.getReviewId());
    }
}
