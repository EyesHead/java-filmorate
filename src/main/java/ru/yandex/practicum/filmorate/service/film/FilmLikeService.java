package ru.yandex.practicum.filmorate.service.film;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.dto.Film;
import ru.yandex.practicum.filmorate.model.responses.success.ResponseMessage;
import ru.yandex.practicum.filmorate.validation.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmLikeService {
    private final FilmStorage filmStorage;

    public ResponseMessage addLikeToFilmByUser(long filmId, long userId) {
        log.info("Получен новый запрос на добавление лайка к фильму с id '{}' от пользователя с id '{}'",
                filmId, userId);
        return filmStorage.addLikeToFilm(filmId, userId);
    }

    public ResponseMessage removeLikeFromFilm(long filmId, long userId) {
        log.info("Получен новый запрос на удаление лайка из фильма с id = {} от пользователя с id = {}",
                filmId, userId);
        return filmStorage.removeLikeFromFilm(filmId, userId);
    }

    public Collection<Film> getMostLikedFilms(
            @Positive(message = "Значение count должно быть положительным")
            Optional<Integer> countOpt
    ) {
        int limit = countOpt.orElse(10);
        log.info("Получен новый запрос на получение {} самых залайканых фильмов", limit);
        Collection<Film> foundFilms = filmStorage.getMostLikedFilms(limit);

        if (foundFilms.isEmpty()) {
            throw new NotFoundException("В сервисе не было найдено ни одного фильма с оценками от пользователей");
        }

        return foundFilms;
    }
}
