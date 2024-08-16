package ru.yandex.practicum.filmorate.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FilmReleaseDateValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilmReleaseDate {
    String message() default "The release date must be a date later than 12/28/1895";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
