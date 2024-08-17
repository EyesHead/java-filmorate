package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.dto.Film;
import ru.yandex.practicum.filmorate.model.responses.success.ResponseMessage;
import ru.yandex.practicum.filmorate.validation.exceptions.NotFoundException;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getAllFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film) throws NotFoundException;

    Collection<Film> getMostLikedFilms(int limit);

    ResponseMessage removeLikeFromFilm(Long filmId, Long userId);

    ResponseMessage addLikeToFilm(Long filmId, Long userId);
}