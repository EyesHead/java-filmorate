package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review saveReview(Review review);

    boolean updateReview(Review review);

    boolean removeReview(long reviewId);

    Optional<Review> getReviewById(long reviewId);

    List<Review> getAllReviews(int amount);

    List<Review> getReviewsByFilmId(long filmId, int amount);

    boolean addLikeDislike(long reviewId, long userId, int likeStatus);

    boolean removeLikeDislike(long reviewId, long userId);
}
