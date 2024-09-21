package ru.yandex.practicum.filmorate.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Builder
@Data
@AllArgsConstructor
public class Genre {
    Long id;
    @NotNull(message = "Genre name is required") String name;
}