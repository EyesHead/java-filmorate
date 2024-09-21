package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.entity.Mpa;
import ru.yandex.practicum.filmorate.service.FilmMpaService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class FilmMpaController {
    private final FilmMpaService mpaService;

    @GetMapping
    public Collection<Mpa> getAllMpaRatings() {
        return mpaService.getAllMpaRatings();
    }

    @GetMapping("/{mpaId}")
    public Mpa getMpaRatingById(@PathVariable @NotNull Long mpaId) {
        return mpaService.getMpaById(mpaId);
    }
}
