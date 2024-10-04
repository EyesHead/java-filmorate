package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Mpa;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.FilmStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class FilmMpaService {
    FilmStorage filmRepository;

    public Collection<Mpa> getAllMpaRatings() {
        log.info("(NEW) Получен новый запрос получение всех рейтингов MPA сервиса");
        Collection<Mpa> MPAs = filmRepository.getAllMpa();
        log.info("(END) Всего было найдено '{}' рейтингов MPA в сервисе", MPAs.size());
        return MPAs;
    }

    public Mpa getMpaById(long mpaId) {
        log.info("(NEW) Получен новый запрос получение MPA рейтинга с MPA ID = {}", mpaId);
        return filmRepository.getMpa(mpaId).orElseThrow(
                () -> new NotFoundException("(END) Рейтинг MPA не найден. Id = " + mpaId));
    }
}
