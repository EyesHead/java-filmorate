package ru.yandex.practicum.filmorate.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.model.NoSpaces;

public class NoSpacesValidator implements ConstraintValidator<NoSpaces, String> {

    @Override
    public void initialize(NoSpaces constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return false;
        }
        return !s.contains(" ");
    }
}
