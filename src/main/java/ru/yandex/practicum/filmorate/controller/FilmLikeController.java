package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.service.FilmLikeService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmLikeController {
    private final FilmLikeService filmLikesService;

    @PutMapping("/{filmId}/like/{userId}")
    public void addLikeToFilm(@PathVariable @NotNull Long filmId,
                              @PathVariable @NotNull Long userId) {
        filmLikesService.addLikeToFilm(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLikeFromFilm(@PathVariable Long filmId,
                                   @PathVariable Long userId) {
        filmLikesService.removeLikeFromFilm(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getMostLikedFilms(@RequestParam(defaultValue = "10") Integer count,
                                              @RequestParam(required = false) Integer genreId,
                                              @RequestParam(required = false) Integer year) {
        return filmLikesService.getMostLikedFilms(count, genreId, year);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> findFilmByQuery(@RequestParam String query, @RequestParam String by) {
        return filmLikesService.getFilmsByQuery(query, by);
    }
}