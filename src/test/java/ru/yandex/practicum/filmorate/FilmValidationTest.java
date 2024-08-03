package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTest {

    private Validator validator;

    private static final LocalDate VALID_RELEASE_DATE = LocalDate.of(2020, 12, 10);
    private static final LocalDate INVALID_RELEASE_DATE = LocalDate.of(1700, 1, 1);

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void validFilmShouldPassValidation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description within the limit.")
                .releaseDate(VALID_RELEASE_DATE)
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Expected no constraint violations, but got: " + violations);
    }

    @Test
    public void filmWithBlankNameShouldFailValidation() {
        Film film = Film.builder()
                .name("")  // Invalid name
                .description("Valid description.")
                .releaseDate(VALID_RELEASE_DATE)
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Expected constraint violations, but got none.");
        assertEquals(1, violations.size(), "Expected 1 constraint violation.");
        assertEquals("не должно быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void filmWithLongDescriptionShouldFailValidation() {
        String longDescription = "A".repeat(201);  // 201 characters long

        Film film = Film.builder()
                .name("Valid Name")
                .description(longDescription)  // Invalid description
                .releaseDate(VALID_RELEASE_DATE)
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Expected constraint violations, but got none.");
        assertEquals(1, violations.size(), "Expected 1 constraint violation.");
        assertEquals("The character limit does not exceed 200", violations.iterator().next().getMessage());
    }

    @Test
    public void filmWithInvalidReleaseDateShouldFailValidation() {
        Film film = Film.builder()
                .name("Valid Name")
                .description("Valid description.")
                .releaseDate(INVALID_RELEASE_DATE)  // Invalid release date
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Expected constraint violations, but got none.");
        assertEquals(1, violations.size(), "Expected 1 constraint violation.");
        assertEquals("Date of film release before 28.12.1895", violations.iterator().next().getMessage());
    }

    @Test
    public void filmWithNegativeDurationShouldFailValidation() {
        Film film = Film.builder()
                .name("Valid Name")
                .description("Valid description.")
                .releaseDate(VALID_RELEASE_DATE)
                .duration(-10)  // Invalid duration
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Expected constraint violations, but got none.");
        assertEquals(1, violations.size(), "Expected 1 constraint violation.");
        assertEquals("Duration cant be negative number", violations.iterator().next().getMessage());
    }
}
