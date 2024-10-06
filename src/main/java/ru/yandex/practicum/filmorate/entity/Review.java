package ru.yandex.practicum.filmorate.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Review {
    @NotNull(message = "Review ID required",
            groups = {Marker.OnUpdate.class})
    long reviewId;

    @NotNull(message = "Film ID required",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    Long filmId;

    @NotNull(message = "User ID required",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    Long userId;

    @NotNull(message = "Review attitude required",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    Boolean isPositive;

    @NotBlank(message = "Content cannot be empty",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    String content;

    int useful;
}
