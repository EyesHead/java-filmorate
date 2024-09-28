package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
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
    public Review addReview(@RequestBody @Validated(Marker.OnCreate.class) Review review) {
        return filmReviewService.addNewReview(review);
    }

    @PutMapping()
    public Review updateReview(@RequestBody @Validated(Marker.OnUpdate.class) Review review) {
        filmReviewService.updateReview(review);
        return review;
    }

    @DeleteMapping("/{reviewId}")
    public void removeReview(@PathVariable @NotNull @Positive long reviewId) {
        filmReviewService.deleteReview(reviewId);
    }

    @GetMapping("/{reviewId}")
    public Review getReviewById(@PathVariable @NotNull @Positive Long reviewId) {
        return filmReviewService.getReview(reviewId);
    }

    @GetMapping()
    public List<Review> getAllReviews(@RequestParam Optional<String> filmId, @RequestParam(defaultValue = "10") String count) {
        return filmReviewService.getFilmReviews(filmId, Integer.parseInt(count));
    }

    @PutMapping("/{id}/like/{userId}")
    public void addReviewLike(@PathVariable @NotNull @Positive long id, @PathVariable @NotNull @Positive long userId) {
        filmReviewService.addReviewLikeDislike(id, userId, 1);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addReviewDislike(@PathVariable @NotNull @Positive long id, @PathVariable @NotNull @Positive long userId) {
        filmReviewService.addReviewLikeDislike(id, userId, 0);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteReviewLike(@PathVariable @NotNull @Positive long id, @PathVariable @NotNull @Positive long userId) {
        filmReviewService.deleteReviewLikeDislike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteReviewDislike(@PathVariable @NotNull @Positive long id, @PathVariable @NotNull @Positive long userId) {
        filmReviewService.deleteReviewLikeDislike(id, userId);
    }
}
