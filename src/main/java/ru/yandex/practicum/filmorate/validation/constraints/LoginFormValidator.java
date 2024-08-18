package ru.yandex.practicum.filmorate.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LoginFormValidator implements ConstraintValidator<LoginForm, String> {

    @Override
    public void initialize(LoginForm constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
        if (str == null) {
            return false;
        }
        return !str.contains(" ");
    }
}
