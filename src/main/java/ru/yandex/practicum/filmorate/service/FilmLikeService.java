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
        filmValidator.checkIsUserAlreadyLikedFilm(filmId, userId);
        userValidator.checkUserOnExist(userId);

        filmStorage.saveLikeToFilm(filmId, userId);
    }

    public void removeLikeFromFilm(long filmId, long userId) {
        log.info("(NEW) Получен запрос от пользователя на удаление лайка из фильма. userId='{}',filmId='{}'", userId, filmId);
        filmStorage.deleteLikeFromFilm(filmId, userId);
    }

    public Collection<Film> getMostLikedFilms(Optional<Integer> countOpt) {
        int limit = countOpt.orElse(10);
        log.info("(NEW) Получен новый запрос на получение {} самых залайканых фильмов", limit);

        return filmStorage.getMostLikedFilms(limit);
    }
}
