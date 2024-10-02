package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.service.FilmRecommendationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{id}/recommendations")
public class FilmRecommendationController {
    private final FilmRecommendationService filmRecommendationService;

    @GetMapping
    public List<Film> getRecommendedFilms(@PathVariable @NotNull long id) {
        return filmRecommendationService.getRecommendedFilms(id);
    }
}
