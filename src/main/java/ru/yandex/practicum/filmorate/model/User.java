package ru.yandex.practicum.filmorate.model;

import jakarta.annotation.PostConstruct;
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
    @NotBlank @Email
    String email;
    @NotBlank @NoSpaces
    String login;
    String name;
    @Past
    LocalDate birthday;

    @PostConstruct
    public void onInitialize() {
        if (name == null || name.isBlank()) {
            name = login;
        }
    }
}
