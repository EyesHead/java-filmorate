package ru.yandex.practicum.filmorate.service.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.entity.Genre;
import ru.yandex.practicum.filmorate.entity.Mpa;
import ru.yandex.practicum.filmorate.exception.InvalidDataRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.FilmStorage;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilmValidator {
    private final FilmStorage filmRepo;

    public void checkFilmMpaRatingOnExist(Mpa mpa) throws InvalidDataRequestException {
        if (mpa == null) {
            log.debug("(Validator) У проверяемого фильма нет mpa рейтинга. Валидация закончена");
            return;
        }
        log.debug("(Validator) Начало проверки MPA рейтинга на существование '{}' в БД.", mpa.getId());

        filmRepo.getMpa(mpa.getId()).orElseThrow(
                () -> new InvalidDataRequestException("MPA рейтинг не найден. Mpa ID = " + mpa.getId())
        );

        log.debug("(Validator) MPA рейтинг с ID='{}' существует. Валидация завершена", mpa.getId());
    }

    public void checkFilmGenresOnExist(Set<Genre> filmGenres) throws InvalidDataRequestException {
        log.debug("(Validator) Начало проверки жанров на существование в БД. {}", filmGenres);

        if (!CollectionUtils.isNotEmpty(filmGenres)) {
            log.debug("(Validator) Фильм не имеет жанров. Валидация завершена");
            return;
        }

        List<Long> allGenreIds = filmRepo.getAllGenres().stream()
                .map(Genre::getId)
                .toList();

        for (Genre requestGenre : filmGenres) {
            if (!allGenreIds.contains(requestGenre.getId())) {
                throw new InvalidDataRequestException(
                        String.format("Жанр с ID='%d' не найден в БД.", requestGenre.getId()));
            }
        }
    }

    public void checkFilmOnExist(long filmId) throws NotFoundException {
        log.debug("(Validator) Валидация фильма на существование в БД. FILM ID = {}", filmId);

        if (filmRepo.getFilmById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм не найден. ID = " + filmId);
        }

        log.debug("(Validator) Фильм существует. Валидация завершена");
    }
}
