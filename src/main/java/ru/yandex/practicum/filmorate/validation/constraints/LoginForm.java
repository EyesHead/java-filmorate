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
@Constraint(validatedBy = LoginFormValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginForm {
    String message() default "must be a well-formed login";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
