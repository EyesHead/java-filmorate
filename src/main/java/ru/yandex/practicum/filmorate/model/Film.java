package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import ru.yandex.practicum.filmorate.constraints.ValidReleaseDate;

import java.time.LocalDate;

/**
 * Film implementation with {@code id}, {@code name},
 * {@code description}, {@code releaseDate} and {@code duration} fields
 * @author Daniil Kuksar
 */
@Data
@Builder(toBuilder = true)
public class Film {
    Long id;
    @NotBlank
    String name;
    @Size(max = 200, message = "The character limit does not exceed 200")
    String description;
    @ValidReleaseDate
    LocalDate releaseDate;
    @Positive(message = "Duration cant be negative number")
    int duration;
}
