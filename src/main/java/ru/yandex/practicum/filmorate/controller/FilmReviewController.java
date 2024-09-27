package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.entity.Marker;
import ru.yandex.practicum.filmorate.entity.Review;
import ru.yandex.practicum.filmorate.service.FilmReviewService;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/reviews")
public class FilmReviewController {
    private final FilmReviewService filmReviewService;

    @PostMapping()
    public Review addReview(@RequestBody @Validated Review review) {
        return filmReviewService.addNewReview(review);
    }

    @PutMapping()
    public Review updateReview(@RequestBody @Validated Review review) {
        filmReviewService.updateReview(review);
        return review;
    }

    @DeleteMapping("/{reviewId}")
    public void removeReview(@PathVariable long reviewId) {
        filmReviewService.deleteReview(reviewId);
    }

    @GetMapping("/{reviewId}")
    public Review getReviewById(@PathVariable Long reviewId) {
        return filmReviewService.getReview(reviewId);
    }

    @GetMapping()
    public List<Review> getAllReviews(@RequestParam Optional<String> filmId, @RequestParam(defaultValue = "10") String count) {
        return filmReviewService.getFilmReviews(filmId, Integer.parseInt(count));
    }

    @PutMapping("/{id}/like/{userId}")
    public void addReviewLike(@PathVariable long id, @PathVariable long userId) {
        filmReviewService.addReviewLikeDislike(id, userId, 1);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addReviewDislike(@PathVariable long id, @PathVariable long userId) {
        filmReviewService.addReviewLikeDislike(id, userId, 0);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteReviewLike(@PathVariable long id, @PathVariable long userId) {
        filmReviewService.deleteReviewLikeDislike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteReviewDislike(@PathVariable long id, @PathVariable long userId) {
        filmReviewService.deleteReviewLikeDislike(id, userId);
    }
}
