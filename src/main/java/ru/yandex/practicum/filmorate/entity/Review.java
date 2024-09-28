package ru.yandex.practicum.filmorate.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Review {
    @NotNull(message = "Review ID required",
            groups = {Marker.OnUpdate.class})
    private long reviewId;

    @NotNull(message = "Film ID required",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private Long filmId;

    @NotNull(message = "User ID required",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private Long userId;

    @NotNull(message = "Review attitude required",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private Boolean isPositive;

    @NotBlank(message = "Content cannot be empty",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String content;

    private int useful;
}
