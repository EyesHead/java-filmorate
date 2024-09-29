package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidDataRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.service.util.DirectorValidator;
import ru.yandex.practicum.filmorate.service.util.FilmValidator;
import ru.yandex.practicum.filmorate.service.util.UserValidator;

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
        log.info("(NEW) Получен новый запрос на получение фильма с ID = {}.", filmId);
        return filmRepo.getFilmById(filmId).orElseThrow(
                () -> new NotFoundException("Фильм не найден. Id = " + filmId)
        );
    }

    public Collection<Film> getAll() {
        log.info("(NEW) Получен новый запрос на получение списка всех фильмов.");
        return filmRepo.getAllFilms();
    }

    public Film create(Film film) {
        log.info("(NEW) Получен новый запрос на создание нового фильма '{}'", film.getName());
        filmValidator.checkFilmGenresOnExist(film.getGenres());
        filmValidator.checkFilmMpaRatingOnExist(film.getMpa());
        return filmRepo.saveFilm(film);
    }

    public Film update(Film film) {
        log.info("(NEW) Получен новый запрос на обновление фильма с ID = {}", film.getId());
        filmValidator.checkFilmGenresOnExist(film.getGenres());

        filmValidator.checkFilmOnExist(film.getId());
        return filmRepo.updateFilm(film);
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        log.info("(NEW) Получен новый запрос на получение общих фильмов пользователей userId='{}',friendId='{}'",
                userId, friendId);

        userValidator.checkUserOnExist(userId);
        userValidator.checkUserOnExist(friendId);

        return filmRepo.getCommonFilms(userId, friendId);
    }

    public List<Film> getSortedFilmsByDirector(long directorId, String sortBy) throws InvalidDataRequestException {
        log.info("(NEW) Получен запрос на получение фильмов с режиссёром '{}', отсортированных по параметру '{}'",
                directorId, sortBy);
        directorValidator.checkDirectorOnExists(directorId);

        return switch (sortBy) {
            case "year" -> filmRepo.getSortedFilmsByDirectorAndReleaseYear(directorId);
            case "likes" -> filmRepo.getSortedFilmsByDirectorAndLikes(directorId);
            default -> throw new InvalidDataRequestException(String
                    .format("Сортировка по параметрам 'режиссёр' и '%s' не предусмотрена", sortBy));
        };
    }

    public void deleteFilmById(long filmId) {
        log.info("(NEW) Получен запрос на удаление фильма с id {} ", filmId);
        filmValidator.checkFilmOnExist(filmId);
        filmRepo.deleteFilmById(filmId);
    }
}