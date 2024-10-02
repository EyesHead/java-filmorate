package ru.yandex.practicum.filmorate.service.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.entity.Genre;
import ru.yandex.practicum.filmorate.entity.Mpa;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
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
            log.debug("У проверяемого фильма нет mpa рейтинга. Проверка закончена");
            return;
        }
        log.debug("Начало проверки MPA рейтинга на существование '{}' в БД.", mpa.getId());

        filmRepo.getMpa(mpa.getId()).orElseThrow(
                () -> new InvalidDataRequestException("MPA не найден. Mpa ID = " + mpa.getId())
        );

        log.debug("MPA рейтинг с ID='{}' существует. Проверка завершена.", mpa.getId());
    }

    public void checkFilmGenresOnExist(Set<Genre> filmGenres) throws InvalidDataRequestException {
        log.debug("Начало проверки жанров на существование в БД. {}", filmGenres);

        if (!CollectionUtils.isNotEmpty(filmGenres)) {
            log.debug("Фильм не имеет жанров. Проверка закончена");
            return;
        }
        // Создаем набор идентификаторов всех жанров для быстрой проверки наличия
        List<Long> allGenreIds = filmRepo.getAllGenres().stream()
                .map(Genre::getId)
                .toList();
        // Проверяем каждый жанр фильма по id с существующими id жанров
        for (Genre requestGenre : filmGenres) {
            if (!allGenreIds.contains(requestGenre.getId())) {
                throw new InvalidDataRequestException(
                        String.format("Жанр с ID='%d' не найден в БД.", requestGenre.getId()));
            }
        }
    }

    public void checkFilmOnExist(long filmId) throws NotFoundException {
        log.debug("Проверка фильма на существование в БД. FILM ID = {}", filmId);

        if (filmRepo.getFilmById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм не найден. ID = " + filmId);
        }
        log.debug("Фильм существует. Проверка завершена.");
    }

    public void checkIsUserAlreadyLikedFilm(long filmId, long userId) throws DuplicatedDataException {
        if (filmRepo.getUsersIdsWhoLikedFilm(filmId).contains(userId)) {
            throw new DuplicatedDataException("User with id = " + userId + " already liked film " + filmId);
        }
    }
}
