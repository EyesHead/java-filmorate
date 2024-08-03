package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Константы для тестов
    private static final LocalDate VALID_BIRTHDAY = LocalDate.of(2000, 1, 1);
    private static final LocalDate INVALID_BIRTHDAY = LocalDate.now().plusDays(1); // Дата в будущем

    @Test
    public void validUserShouldPassValidation() {
        User user = User.builder()
                .email("valid@example.com")
                .login("validLogin")
                .name("Valid Name")
                .birthday(VALID_BIRTHDAY)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Expected no constraint violations, but got: " + violations);
    }

    @Test
    public void userWithEmptyEmailShouldFailValidation() {
        User user = User.builder()
                .email("") // Неправильный email
                .login("validLogin")
                .name("Valid Name")
                .birthday(VALID_BIRTHDAY)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Expected constraint violations, but got none.");
        assertEquals(1, violations.size(), "Expected 1 constraint violation.");
        assertEquals("не должно быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void userWithInvalidEmailShouldFailValidation() {
        User user = User.builder()
                .email("invalidemail.com") // Неправильный email
                .login("validLogin")
                .name("Valid Name")
                .birthday(VALID_BIRTHDAY)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Expected constraint violations, but got none.");
        assertEquals(1, violations.size(), "Expected 1 constraint violation.");
        assertEquals("должно иметь формат адреса электронной почты", violations.iterator().next().getMessage());
    }

    @Test
    public void userWithEmptyLoginShouldFailValidation() {
        User user = User.builder()
                .email("valid@example.com")
                .login("") // Неправильный login
                .name("Valid Name")
                .birthday(VALID_BIRTHDAY)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Expected constraint violations, but got none.");
        assertEquals(1, violations.size(), "Expected 1 constraint violation.");
        assertEquals("не должно быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void userWithLoginContainingSpacesShouldFailValidation() {
        User user = User.builder()
                .email("valid@example.com")
                .login("invalid login") // Неправильный login
                .name("Valid Name")
                .birthday(VALID_BIRTHDAY)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Expected constraint violations, but got none.");
        assertEquals(1, violations.size(), "Expected 1 constraint violation.");
        assertEquals("must not contain spaces", violations.iterator().next().getMessage());
    }

    @Test
    public void userWithFutureBirthdayShouldFailValidation() {
        User user = User.builder()
                .email("valid@example.com")
                .login("validLogin")
                .name("Valid Name")
                .birthday(INVALID_BIRTHDAY) // Неправильная дата рождения
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Expected constraint violations, but got none.");
        assertEquals(1, violations.size(), "Expected 1 constraint violation.");
        assertEquals("должно содержать прошедшую дату", violations.iterator().next().getMessage());
    }
}
