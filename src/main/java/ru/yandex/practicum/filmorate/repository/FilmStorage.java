package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.entity.Genre;
import ru.yandex.practicum.filmorate.entity.Mpa;

import java.util.*;


public interface FilmStorage {
    Collection<Film> getAllFilms();

    Optional<Film> getFilmById(long filmId);

    Film saveFilm(Film film);

    Film updateFilm(Film film);

    void saveLikeToFilm(long filmId, long userId);

    void deleteLikeFromFilm(long filmId, long userId);

    Collection<Film> getMostLikedFilms(int limit);

    Collection<Film> getMostLikedFilmsByGenreAndYear(int limit, int genreId, int year);

    Collection<Film> getMostLikedFilmsByGenreOrYear(Integer limit, Integer genreId, Integer year);

    Collection<Long> getUsersIdsWhoLikedFilm(long filmId);

    Collection<Mpa> getAllMpa();

    Collection<Genre> getAllGenres();

    Optional<Genre> getGenre(long genreId);

    Optional<Mpa> getMpa(long mpaId);

    void deleteFilmById(long filmId);

    List<Film> getSortedByReleaseDateFilmsOfDirector(long directorId);

    List<Film> getSortedByLikesFilmsOfDirector(long directorId);

    List<Film> getCommonFilms(long userId, long friendId);
}