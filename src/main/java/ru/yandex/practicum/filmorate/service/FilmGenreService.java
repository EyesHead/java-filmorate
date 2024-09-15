package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Genre;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.FilmStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FilmGenreService {
    FilmStorage filmStorage;

    public Collection<Genre> getAllGenres() {
        return filmStorage.getAllGenres();
    }

    public Genre getGenreById(long genreId) {
        return filmStorage.getGenre(genreId).orElseThrow(
                () -> new NotFoundException("Genre was not found. Id = " + genreId));
    }
}
