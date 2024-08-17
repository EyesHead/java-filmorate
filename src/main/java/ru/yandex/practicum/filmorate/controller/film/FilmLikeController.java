package ru.yandex.practicum.filmorate.controller.film;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.dto.Film;
import ru.yandex.practicum.filmorate.model.responses.success.ResponseMessage;
import ru.yandex.practicum.filmorate.service.film.FilmLikeService;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmLikeController {
    private final FilmLikeService filmLikesService;

    @PutMapping("/{id}/like/{userId}")
    public ResponseMessage addLikeToFilm(@PathVariable(name = "id") Long filmId,
                                         @PathVariable(name = "userId") Long userId) {
        return filmLikesService.addLikeToFilmByUser(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseMessage removeLikeFromFilm(@PathVariable(name = "id") Long filmId,
                                              @PathVariable(name = "userId") Long userId) {
        return filmLikesService.removeLikeFromFilm(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getMostLikedFilms(@RequestParam Optional<Integer> count) {
        return filmLikesService.getMostLikedFilms(count);
    }
}