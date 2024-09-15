package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Genre;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.FilmStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class FilmGenreService {
    FilmStorage filmStorage;

    public Collection<Genre> getAllGenres() {
        log.info("(NEW) Получен новый запрос получение всех жанров сервиса");

        return filmStorage.getAllGenres();
    }

    public Genre getGenreById(long genreId) {
        log.info("Получен новый запрос получение жанра фильма с ID жанра = {}", genreId);

        return filmStorage.getGenre(genreId).orElseThrow(
                () -> new NotFoundException("Genre was not found. Id = " + genreId));
    }
}
