package ru.yandex.practicum.filmorate.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    private long reviewId;

    @NotNull(message = "Film ID required")
    private Long filmId;

    @NotNull(message = "User ID required")
    private Long userId;

    @NotNull
    @JsonProperty(value = "isPositive")
    private Boolean isPositive;

    @NotBlank(message = "Content cannot be empty")
    private String content;

    private int useful;

    public Review(String content, boolean isPositive, long userId, long filmId) {
        this.filmId = filmId;
        this.userId = userId;
        this.isPositive = isPositive;
        this.content = content;
    }
}
