package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.service.util.FilmValidator;
import ru.yandex.practicum.filmorate.service.util.UserValidator;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmLikeService {
    private final FilmStorage filmStorage;
    private final FilmValidator filmValidator;
    private final UserValidator userValidator;

    public void addLikeToFilm(long filmId, long userId) {
        log.info("(NEW) Получен запрос от пользователя на добавление лайка к фильму. userId='{}',filmId='{}'",
                userId, filmId);

        filmValidator.checkFilmOnExist(filmId);
        userValidator.checkUserOnExist(userId);

        filmValidator.checkIsUserAlreadyLikedFilm(filmId, userId);

        filmStorage.saveLikeToFilm(filmId, userId);
    }

    public void removeLikeFromFilm(long filmId, long userId) {
        log.info("(NEW) Получен запрос от пользователя на удаление лайка из фильма. userId='{}',filmId='{}'", userId, filmId);

        filmValidator.checkFilmOnExist(filmId);
        userValidator.checkUserOnExist(userId);

        filmStorage.deleteLikeFromFilm(filmId, userId);
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
        String text = query.toLowerCase();
        return switch (text) {
            case "title" -> filmStorage.filmsSearch(text, null);
            case "director" -> filmStorage.filmsSearch(null, text);
            default -> filmStorage.filmsSearch(text, text);
        };
    }
}
