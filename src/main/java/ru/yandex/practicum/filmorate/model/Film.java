package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.Duration;
import java.time.Instant;

/**
 * Film implementation with {@code id}, {@code name},
 * {@code description}, {@code releaseDate} and {@code duration} fields
 * @author Daniil Kuksar
 */
@Value
@Builder(toBuilder = true)
public class Film {
    Integer id;
    String name;
    String description;
    Instant releaseDate;
    Duration duration;
}
