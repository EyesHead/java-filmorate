package ru.yandex.practicum.filmorate.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Film implementation with {@code id}, {@code name},
 * {@code description}, {@code releaseDate} and {@code duration} fields
 * @author Daniil Kuksar
 */
@Data
@Builder(toBuilder = true)
public class Film {
    Long id;
    @NotBlank String name;
    @Size(max = 200) String description;
    LocalDate releaseDate;
    @Positive int duration;

}
