package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.dto.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrudFilmService {
    private final FilmStorage filmRepo;

    public Collection<Film> getAll() {
        log.info("Пользователь отправил запрос на получение списка всех фильмов");
        return filmRepo.getAllFilms();
    }

    public Film create(Film film) {
        log.info("Пользователь оптарвил запрос на создание нового фильма {}", film.getName());
        return filmRepo.addFilm(film);
    }

    public Film update(Film film) {
        log.info("Пользователь отправил запрос на обновление данных о фильме {}", film.getName());
        return filmRepo.updateFilm(film);
    }
}