package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.repository.EventLogger;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.service.validators.FilmValidator;
import ru.yandex.practicum.filmorate.service.validators.UserValidator;

import java.util.*;

import static ru.yandex.practicum.filmorate.entity.EventOperation.ADD;
import static ru.yandex.practicum.filmorate.entity.EventOperation.REMOVE;
import static ru.yandex.practicum.filmorate.entity.EventType.LIKE;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmLikeService {
    private final FilmStorage filmStorage;
    private final FilmValidator filmValidator;
    private final UserValidator userValidator;
    private final EventLogger eventLogger;

    public void addLikeToFilm(long filmId, long userId) {
        log.info("(NEW) Получен запрос от пользователя на добавление лайка к фильму. userId='{}',filmId='{}'",
                userId, filmId);

        filmValidator.checkFilmOnExist(filmId);
        userValidator.checkUserOnExist(userId);

        filmStorage.saveLikeToFilm(filmId, userId);
        eventLogger.logEvent(userId, LIKE, ADD, filmId);
    }

    public void removeLikeFromFilm(long filmId, long userId) {
        log.info("(NEW) Получен запрос от пользователя на удаление лайка из фильма. userId='{}',filmId='{}'", userId, filmId);

        filmValidator.checkFilmOnExist(filmId);
        userValidator.checkUserOnExist(userId);

        filmStorage.deleteLikeFromFilm(filmId, userId);
        eventLogger.logEvent(userId, LIKE, REMOVE, filmId);
    }

    public Collection<Film> getMostLikedFilms(Integer count, Integer genreId, Integer year) {
        log.info("(NEW) Получен новый запрос на получение {} самых залайканых фильмов жанра {} года {}", count, genreId, year);
        if (genreId != null & year != null) {
            return filmStorage.getMostLikedFilmsByGenreAndYear(count, genreId, year);
        } else if (genreId == null & year == null) {
            return filmStorage.getMostLikedFilms(count);
        } else {
            return filmStorage.getMostLikedFilmsByGenreOrYear(count, genreId, year);
        }
    }

    public Collection<Film> filmsSearch(String query, String by) {
        log.debug("Получен поиск фильмов query: {} by: {}", query, by);
        String text = query.toLowerCase();
        return switch (by) {
            case "title" -> filmStorage.filmsSearch(text, null);
            case "director" -> filmStorage.filmsSearch(null, text);
            default -> filmStorage.filmsSearch(text, text);
        };
    }
}
