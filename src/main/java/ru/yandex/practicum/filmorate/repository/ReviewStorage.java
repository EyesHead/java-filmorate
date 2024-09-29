package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review addReview(Review review);

    void updateReview(Review review);

    void removeReview(long reviewId);

    Optional<Review> getReviewById(long reviewId);

    List<Review> getAllReviews(int amount);

    List<Review> getReviewsByFilmId(long filmId, int amount);

    void addLikeDislike(long reviewId, long userId, int likeStatus);

    void removeLikeDislike(long reviewId, long userId);
}
