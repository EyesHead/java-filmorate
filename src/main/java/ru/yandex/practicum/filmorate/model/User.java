package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.constraints.NoSpaces;

import java.time.LocalDate;

/**
 * Immutable User implementation with {@code @Builder} and
 * {@code id}, {@code email}, {@code login}, {@code name} fields
 * @author Daniil Kuksar
 */
@Data
@Builder(toBuilder = true, builderClassName = "UserBuilder")
public class User {
    Long id;
    @Email(message = "must be in email address format")
    @NotBlank(message = "must not be blank")
    String email;
    @NoSpaces @NotBlank(message = "must not be blank")
    String login;
    String name;
    @Past(message = "must contain a past date")
    LocalDate birthday;
}
