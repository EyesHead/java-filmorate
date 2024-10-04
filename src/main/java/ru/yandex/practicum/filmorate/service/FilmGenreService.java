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
        log.info("(NEW) Получен запрос на получение всех жанров сервиса.");

        Collection<Genre> genres = filmStorage.getAllGenres();
        log.info("(END) Получены все жанры сервиса. Количество жанров: {}", genres.size());
        return genres;
    }

    public Genre getGenreById(long genreId) {
        log.info("(NEW) Получен запрос на получение жанра фильма с ID = {}", genreId);

        Genre foundGenre = filmStorage.getGenre(genreId).orElseThrow(
                () -> new NotFoundException("Жанр не найден. Id = " + genreId)
        );
        log.info("(END) Жанр с ID = {} успешно получен: {}", genreId, foundGenre);
        return foundGenre;
    }
}
