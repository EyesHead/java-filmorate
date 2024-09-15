package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Mpa;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.FilmStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FilmMpaService {
    private final FilmStorage filmRepository;

    public Mpa getMpaById(long mpaId) {
        return filmRepository.getMpa(mpaId).orElseThrow(
                () -> new NotFoundException("Mpa rating not found. Id = " + mpaId));
    }

    public Collection<Mpa> getAllMpaRatings() {
        return filmRepository.getAllMpa();
    }
}
