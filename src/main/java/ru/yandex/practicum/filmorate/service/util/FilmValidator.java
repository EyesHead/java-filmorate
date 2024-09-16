package ru.yandex.practicum.filmorate.service.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.entity.Genre;
import ru.yandex.practicum.filmorate.entity.Mpa;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Slf4j
@Component
public class FilmValidator {
    private FilmStorage filmRepo;


    public void checkFilmMpaRatingOnExist(Mpa mpa) throws ValidationException {
        if (mpa == null) return;

        log.debug("Проверка MPA рейтинга на существование '{}' в БД.", mpa.getId());

        filmRepo.getMpa(mpa.getId()).orElseThrow(
                () -> new ValidationException("MPA не найден. Mpa ID = " + mpa.getId())
        );

        log.debug("MPA рейтинг с ID='{}' существует. Проверка завершена.", mpa.getId());
    }

    public void checkFilmGenresOnExist(Set<Genre> filmGenres) throws ValidationException {
        log.debug("Проверка на существование жанров в БД. {}", filmGenres);

        if (filmGenres == null || filmGenres.isEmpty()) {
            log.debug("Фильм не имеет жанров.");
            return;
        }

        List<Genre> allGenres = (List<Genre>) filmRepo.getAllGenres();
        // Создаем набор идентификаторов всех жанров для быстрой проверки наличия
        List<Long> allGenreIds = allGenres.stream()
                .map(Genre::getId)
                .toList();

        // Проверяем каждый жанр фильма
        for (Genre requestGenre : filmGenres) {
            if (!allGenreIds.contains(requestGenre.getId())) {
                throw new ValidationException(
                        String.format("Жанр с ID='%d' не найден в БД", requestGenre.getId()));
            }
        }
    }

    public void checkFilmOnExist(long filmId) throws ValidationException {
        log.debug("Проверка фильма на существование в БД. FILM ID = {}", filmId);

        if (filmRepo.getFilmById(filmId).isEmpty()) {
            throw new ValidationException("Фильм не найден. ID = " + filmId);
        }
        log.debug("Фильм существует. Проверка завершена.");
    }

    public void checkIsUserAlreadyLikedFilm(long filmId, long userId) throws ValidationException {
        if (filmRepo.getUsersIdsWhoLikedFilm(filmId).contains(userId)) {
            throw new ValidationException("User with id = " + userId + " already liked film " + filmId);
        }
    }
}
