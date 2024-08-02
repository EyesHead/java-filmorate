package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Immutable User implementation with {@code @Builder} and
 * {@code id}, {@code email}, {@code login}, {@code name} fields
 * @author Daniil Kuksar
 */
@Data
@Builder(toBuilder = true)
public class User {
    Long id;
    @Email String email;
    @NotBlank String login;
    String name;
}
