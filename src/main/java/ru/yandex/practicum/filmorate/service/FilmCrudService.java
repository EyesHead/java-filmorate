package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.exception.InvalidDataRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.service.validators.DirectorValidator;
import ru.yandex.practicum.filmorate.service.validators.FilmValidator;
import ru.yandex.practicum.filmorate.service.validators.UserValidator;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmCrudService {
    private final FilmStorage filmRepo;
    private final FilmValidator filmValidator;
    private final UserValidator userValidator;
    private final DirectorValidator directorValidator;

    public Film getFilmById(long filmId) {
        log.info("(NEW) Получен запрос на получение фильма с ID = {}.", filmId);

        Film foundFilm = filmRepo.getFilmById(filmId).orElseThrow(
                () -> new NotFoundException("Фильм не найден. Id = " + filmId)
        );
        log.info("(END) Фильм с ID = {} был найден: {}", filmId, foundFilm);
        return foundFilm;
    }

    public Collection<Film> getAll() {
        log.info("(NEW) Получен запрос на получение списка всех фильмов.");

        Collection<Film> films = filmRepo.getAllFilms();
        log.info("(END) Список всех фильмов успешно получен. Количество: {}", films.size());
        return films;
    }

    public Film create(Film film) {
        log.info("(NEW) Получен запрос на создание нового фильма '{}'", film.getName());

        filmValidator.checkFilmGenresOnExist(film.getGenres());
        filmValidator.checkFilmMpaRatingOnExist(film.getMpa());

        Film createdFilm = filmRepo.saveFilm(film);
        log.info("(END) Фильм '{}' был успешно создан: {}", film.getName(), createdFilm);
        return createdFilm;
    }

    public Film update(Film film) {
        log.info("(NEW) Получен запрос на обновление фильма с ID = {}", film.getId());

        filmValidator.checkFilmGenresOnExist(film.getGenres());
        filmValidator.checkFilmOnExist(film.getId());

        Film updatedFilm = filmRepo.updateFilm(film);
        log.info("(END) Фильм с ID = {} был успешно обновлён: {}", film.getId(), updatedFilm);
        return updatedFilm;
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        log.info("(NEW) Получен запрос на получение общих фильмов пользователей userId = '{}', friendId = '{}'", userId, friendId);

        userValidator.checkUserOnExist(userId);
        userValidator.checkUserOnExist(friendId);

        List<Film> commonFilms = filmRepo.getCommonFilms(userId, friendId);
        log.info("(END) Общие фильмы пользователей userId = '{}', friendId = '{}' успешно получены. " +
                "Количество фильмов: {}", userId, friendId, commonFilms.size());
        return commonFilms;
    }

    public List<Film> getSortedFilmsByDirector(long directorId, String sortBy) throws InvalidDataRequestException {
        log.info("(NEW) Получен запрос на получение фильмов " +
                "с режиссёром с ID = '{}' отсортированных по параметру '{}'", directorId, sortBy);

        directorValidator.checkDirectorOnExists(directorId);

        List<Film> sortedFilms;
        switch (sortBy) {
            case "year" -> {
                sortedFilms = filmRepo.getSortedByReleaseDateFilmsOfDirector(directorId);
                log.info("(END) Фильмы режиссёра с ID = '{}' успешно отсортированы по году выпуска. " +
                        "Количество фильмов: {}", directorId, sortedFilms.size());
            }
            case "likes" -> {
                sortedFilms = filmRepo.getSortedByLikesFilmsOfDirector(directorId);
                log.info("(END) Фильмы режиссёра с ID = '{}' успешно отсортированы по количеству лайков. " +
                        "Количество фильмов: {}", directorId, sortedFilms.size());
            }
            default -> throw new InvalidDataRequestException(String.format("Сортировка по параметрам " +
                    "'режиссёр' и '%s' не предусмотрена", sortBy));
        }
        return sortedFilms;
    }

    public void deleteFilmById(long filmId) {
        log.info("(NEW) Получен запрос на удаление фильма с id {} ", filmId);
        if (filmRepo.deleteFilmById(filmId)) {
            log.info("(END) Фильм с ID = {} успешно удалён.", filmId);
        } else {
            log.info("(END) Фильм с ID = {} не был найден/не удалён", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не удалён, т.к. не найден");
        }
    }
}