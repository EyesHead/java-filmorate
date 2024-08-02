package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Objects;

@Slf4j(topic = "Films controller log")
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("(GET) request by client received");
        log.trace("User requested movies: {}", films.values());
        return films.values();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("(POST) request by client received");
        validateOnReleaseDate(film);
        Film newFilm = getNewFilmByData(film);
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("(PUT) request by client received");
        validateOnReleaseDate(film);
        boolean isFilmUpdatable = films.keySet().stream()
                .filter(Objects::nonNull)
                .anyMatch(oldFilmId -> film.getId() != null && film.getId().equals(oldFilmId));

        if (isFilmUpdatable) {
            films.put(film.getId(), film);
            log.debug("(PUT) film was found in memory and updated: {}", film);
            log.info("(PUT) Film was successfully updated");
            return film;
        } else {
            log.debug("(PUT) film is not additive: {}", film);
            Film newFilm = getNewFilmByData(film);
            films.put(newFilm.getId(), newFilm);
            log.info("(PUT) Film was not defined, new created");
            return newFilm;
        }
    }

    private Film getNewFilmByData(Film filmData) {
        Film newFilm = filmData.toBuilder()
                .id(generateUniqueId()).build();
        log.info("New film was created");
        log.trace("New film was created: {}", newFilm);
        return newFilm;
    }

    private static void validateOnReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(
                LocalDate.of(1895, 12, 28))) {
            log.info("User entered incorrect release date for film");
            throw new ValidationException("Date of film release before 28.12.1895");
        }
        log.info("Validation on release Date passed successfully");
        log.trace("Film with current releaseDate passed the validation: {}", film);
    }

    private long generateUniqueId() {
        long maxId = films.keySet().stream()
                .mapToLong(Long::longValue)
                .max().orElse(0L);
        log.trace("Max film id in memory={}", maxId);
        return ++maxId;
    }
}
