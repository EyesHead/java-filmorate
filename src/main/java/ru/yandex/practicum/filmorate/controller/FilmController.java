package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.serviceRepo.FilmorateRepository;
import ru.yandex.practicum.filmorate.serviceRepo.FilmsServiceRepo;
import java.util.Collection;

@Slf4j(topic = "Films controller log")
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    FilmorateRepository<Film> filmService;

    @Autowired
    FilmController(FilmsServiceRepo filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("GET request by client received");
        return filmService.getAll();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("POST request by client received");
        return filmService.create(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("PUT request by client received");
        return filmService.update(film);
    }
}
