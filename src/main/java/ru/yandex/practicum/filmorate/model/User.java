package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/**
 * Immutable User implementation with {@code @Builder} and
 * {@code id}, {@code email}, {@code login}, {@code name} fields
 * @author Daniil Kuksar
 */
@Value
@Builder(toBuilder = true, builderClassName = "UserBuilder")
public class User {
    Long id;
    @NotBlank @Email String email;
    @NotBlank String login;
    String name;
    @Past LocalDate birthday;
}
