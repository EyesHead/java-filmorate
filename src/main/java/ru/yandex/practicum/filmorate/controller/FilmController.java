package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
        log.info("POST request by client received");
        validateOnCreate(film);
        Film newFilm = getNewFilmByData(film);
        films.put(newFilm.getId(), newFilm);
        log.trace("POST new film: {}", newFilm);
        log.info("POST film was created with ID='{}'", newFilm.getId());
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("(PUT) request by client received");
        validateOnUpdate(film);
        films.put(film.getId(), film);
        log.debug("PUT film was found in memory and updated: {}", film);
        log.info("PUT film was found in memory and updated with ID = '{}'", film.getId());
        return film;

    }
    private void validateOnUpdate(Film film) {
        if (film.getId() == null) {
            log.debug("ID from request is null");
            throw new ValidationException("ID cannot be null for update");
        }

        validateOnReleaseDate(film);

        films.keySet().stream()
                .filter(Objects::nonNull)
                .filter(oldFilmId -> film.getId().equals(oldFilmId))
                .findFirst()
                .orElseThrow(() -> {
                    log.debug("Film not found in memory: ID={}", film.getId());
                    return new ValidationException("Film was not found in memory");
                });
    }

    private void validateOnCreate(Film film) {
        validateOnReleaseDate(film);
        log.info("POST user was successfully validated on create");
    }

    private static void validateOnReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(
                LocalDate.of(1895, 12, 28))) {
            log.info("incorrect release date for film");
            throw new ValidationException("Date of film release before 28.12.1895");
        }
    }


    private Film getNewFilmByData(Film filmData) {
        Film newFilm = filmData.toBuilder()
                .id(generateUniqueId()).build();
        log.info("New film was created");
        log.trace("New film was created: {}", newFilm);
        return newFilm;
    }

    private long generateUniqueId() {
        long maxId = films.keySet().stream()
                .mapToLong(Long::longValue)
                .max().orElse(0L);
        log.trace("Max film id in memory={}", maxId);
        return ++maxId;
    }
}
