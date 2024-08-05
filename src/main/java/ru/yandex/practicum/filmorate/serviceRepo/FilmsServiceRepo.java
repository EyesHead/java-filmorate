package ru.yandex.practicum.filmorate.serviceRepo;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class FilmsServiceRepo implements FilmorateRepository<Film> {
    Map<Long, Film> films = new HashMap<>();

    public Collection<Film> getAll() {
        log.trace("User requested movies: {}", films.values());
        return films.values();
    }

    @Override
    public Film create(Film film) {
        Film newFilm = getNewFilmByData(film);
        films.put(newFilm.getId(), newFilm);
        log.trace("POST new film: {}", newFilm);
        log.info("POST film was created with ID='{}'", newFilm.getId());
        return newFilm;
    }

    @Override
    public Film update(Film film) {
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

        films.keySet().stream()
                .filter(Objects::nonNull)
                .filter(oldFilmId -> film.getId().equals(oldFilmId))
                .findFirst()
                .orElseThrow(() -> {
                    log.debug("Film not found in memory: ID={}", film.getId());
                    return new ValidationException("Film was not found in memory");
                });
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
