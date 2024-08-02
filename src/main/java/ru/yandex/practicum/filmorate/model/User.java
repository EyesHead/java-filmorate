package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;

/**
 * Immutable User implementation with {@code @Builder} and
 * {@code id}, {@code email}, {@code login}, {@code name} fields
 * @author Daniil Kuksar
 */
@Value
@Builder(toBuilder = true)
public class User {
    Integer id;
    String email;
    String login;
    String name;
}
