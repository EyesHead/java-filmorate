package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InvalidDataRequestException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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
        try {
            filmValidator.checkFilmOnExist(filmId);
            userValidator.checkUserOnExist(userId);
        } catch (ValidationException e) {
            throw new InvalidDataRequestException(e.getMessage());
        }

        try {
            filmValidator.checkIsUserAlreadyLikedFilm(filmId, userId);
        } catch (ValidationException e) {
            throw new DuplicatedDataException(e.getMessage());
        }

        filmStorage.saveLikeToFilm(filmId, userId);
    }

    public void removeLikeFromFilm(long filmId, long userId) {
        log.info("(NEW) Получен запрос от пользователя на удаление лайка из фильма. userId='{}',filmId='{}'", userId, filmId);

        try {
            filmValidator.checkFilmOnExist(filmId);
            userValidator.checkUserOnExist(userId);
        } catch (ValidationException e) {
            throw new InvalidDataRequestException(e.getMessage());
        }

        filmStorage.deleteLikeFromFilm(filmId, userId);
    }

    public Collection<Film> getMostLikedFilms(Optional<Integer> countOpt) {
        int limit = countOpt.orElse(10);
        log.info("(NEW) Получен новый запрос на получение {} самых залайканых фильмов", limit);

        return filmStorage.getMostLikedFilms(limit);
    }
}
