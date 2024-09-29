package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.service.FilmCrudService;
import ru.yandex.practicum.filmorate.entity.Marker;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Validated
public class FilmCrudController {
    private final FilmCrudService filmService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> getAllFilms() {
        return filmService.getAll();
    }

    @GetMapping("{filmId}")
    @ResponseStatus(HttpStatus.OK)
    public Film getFilmById(@NotNull
                            @Positive
                            @PathVariable
                            Long filmId) {
        return filmService.getFilmById(filmId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@RequestBody
                           @Validated(Marker.OnCreate.class)
                           Film film) {
        return filmService.create(film);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@RequestBody
                           @Validated(Marker.OnUpdate.class)
                           Film film) {
        return filmService.update(film);
    }

    @GetMapping("/common")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getCommonFilms(@RequestParam(name = "userId") long userId,
                                     @RequestParam(name = "friendId") long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @DeleteMapping("{filmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilmById(@PathVariable long filmId) {
        filmService.deleteFilmById(filmId);
    }
}
