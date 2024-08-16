package ru.yandex.practicum.filmorate.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to validate that a string does not contain spaces.
 */
@Constraint(validatedBy = NoSpacesValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoSpaces {
    String message() default "must not contain spaces";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
