package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.repository.EventLogger;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.service.validators.FilmValidator;
import ru.yandex.practicum.filmorate.service.validators.UserValidator;

import java.util.Collection;

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
        log.info("(NEW) Получен запрос на добавление лайка к фильму. userId='{}', filmId='{}'", userId, filmId);

        filmValidator.checkFilmOnExist(filmId);
        userValidator.checkUserOnExist(userId);

        if (filmStorage.saveLikeToFilm(filmId, userId)) {
            eventLogger.logEvent(userId, LIKE, ADD, filmId);
            log.info("(END) Лайк успешно добавлен. userId='{}', filmId='{}'", userId, filmId);
        } else {
            log.warn("(END) Не удалось добавить лайк. userId='{}', filmId='{}'", userId, filmId);
        }
    }

    public void removeLikeFromFilm(long filmId, long userId) {
        log.info("(NEW) Получен запрос на удаление лайка с фильма. userId='{}', filmId='{}'", userId, filmId);

        filmValidator.checkFilmOnExist(filmId);
        userValidator.checkUserOnExist(userId);

        if (filmStorage.deleteLikeFromFilm(filmId, userId)) {
            eventLogger.logEvent(userId, LIKE, REMOVE, filmId);
            log.info("(END) Лайк успешно удален. userId='{}', filmId='{}'", userId, filmId);
        } else {
            log.warn("(END) Не удалось удалить лайк. userId='{}', filmId='{}'", userId, filmId);
        }
    }

    public Collection<Film> getMostLikedFilms(Integer count, Integer genreId, Integer year) {
        if (genreId != null && year != null) {
            log.info("(NEW) Получен запрос на получение {} самых залайканных фильмов, отфильтрованных по жанру {} и году {}", count, genreId, year);
            Collection<Film> films = filmStorage.getMostLikedFilmsByGenreAndYear(count, genreId, year);
            log.info("(END) Возвращено {} фильмов, отфильтрованных по жанру и году", films.size());
            return films;
        } else if (genreId == null && year == null) {
            log.info("(NEW) Получен запрос на получение {} самых залайканных фильмов", count);
            Collection<Film> films = filmStorage.getMostLikedFilms(count);
            log.info("(END) Возвращено {} самых залайканных фильмов", films.size());
            return films;
        } else {
            log.info("(NEW) Получен запрос на получение {} самых залайканных фильмов, отфильтрованных по году '{}' или жанру '{}'", count, year, genreId);
            Collection<Film> films = filmStorage.getMostLikedFilmsByGenreOrYear(count, genreId, year);
            log.info("(END) Возвращено {} фильмов, отфильтрованных по одному из параметров", films.size());
            return films;
        }
    }

    public Collection<Film> getFilmsByQuery(String query, String by) {
        String text = query.toLowerCase();
        switch (by) {
            case "title" -> {
                log.info("(NEW) Получен запрос на поиск фильмов по названию/части названия: {}", text);
                Collection<Film> films = filmStorage.getFilmsByQuery(text, null);
                log.info("(END) Найдено {} фильмов по названию/части названия", films.size());
                return films;
            }
            case "director" -> {
                log.info("(NEW) Получен запрос на поиск фильмов по режиссёру: {}", text);
                Collection<Film> films = filmStorage.getFilmsByQuery(null, text);
                log.info("(END) Найдено {} фильмов по режиссёру", films.size());
                return films;
            }
            default -> {
                log.info("(NEW) Получен запрос на поиск фильмов по названию/части названия и режиссёру: {}", text);
                Collection<Film> films = filmStorage.getFilmsByQuery(text, text);
                log.info("(END) Найдено {} фильмов по обоим параметрам", films.size());
                return films;
            }
        }
    }
}
