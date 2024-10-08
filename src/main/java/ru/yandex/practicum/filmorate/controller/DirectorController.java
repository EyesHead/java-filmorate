package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.entity.Director;
import ru.yandex.practicum.filmorate.entity.Marker;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @PutMapping
    public Director updateDirector(@RequestBody @Validated(Marker.OnUpdate.class) Director director) {
        return directorService.updateDirector(director);
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@Positive @PathVariable(name = "id") Long directorId) {
        return directorService.getDirectorById(directorId);
    }

    @PostMapping
    public Director createDirector(@RequestBody @Validated(Marker.OnCreate.class) Director director) {
        return directorService.createDirector(director);
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        return directorService.getAllDirectors();
    }

    @DeleteMapping("/{id}")
    public void removeDirector(@Positive @PathVariable(name = "id") Long directorId) {
        directorService.removeDirector(directorId);
    }
}