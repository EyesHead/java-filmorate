package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.entity.Genre;
import ru.yandex.practicum.filmorate.service.FilmGenreService;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class FilmGenresController {
    private final FilmGenreService genresService;

    @GetMapping
    public Collection<Genre> getAllGenres() {
        return genresService.getAllGenres();
    }

    @GetMapping("/{genreId}")
    public Genre getGenreById(@PathVariable @NotNull Long genreId) {
        return genresService.getGenreById(genreId);
    }
}
