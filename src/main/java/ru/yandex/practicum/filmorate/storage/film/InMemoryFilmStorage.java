package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.dto.User;
import ru.yandex.practicum.filmorate.model.responses.success.ResponseMessage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.dto.Film;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {
    private final UserStorage userStorage;

    private final Map<Long, Film> films = new HashMap<>();

    public Collection<Film> getAllFilms() {
        log.trace("Запрашиваемый список фильмов: {}", films.values());
        return films.values();
    }

    @Override
    public Film addFilm(Film film) {
        Film newFilm = film.toBuilder()
                .id(generateUniqueId())
                .build();

        films.put(newFilm.getId(), newFilm);
        log.trace("Фильм успешно создан и добавлен : {}", newFilm);
        log.info("Фильм \"{}\" был успешно создан и добавлен в репозиторий", newFilm.getName());
        return newFilm;
    }

    @Override
    public Film updateFilm(Film film) throws NotFoundException {
        checkFilmOnExistAndGet(film.getId());

        Film newFilm = film.toBuilder()
                .usersLikes(films.get(film.getId()).getUsersLikes())
                .build();

        films.put(newFilm.getId(), newFilm);
        log.debug("Фильм был успешно найден и обновлён: {}", newFilm);
        log.info("Данные о фильме с id =\"{}\" успешно обновлены", newFilm.getId());
        return newFilm;
    }

    @Override
    public Collection<Film> getMostLikedFilms(int limit) {
        if (films.isEmpty()) {
            throw new NotFoundException("Пока что в сервис не было добавлено ни одного фильма");
        }

        Collection<Film> foundFilms = films.values().stream()
                .filter(film -> !film.getUsersLikes().isEmpty())
                .sorted(Comparator.comparingInt(film -> ((Film) film).getUsersLikes().size()).reversed())
                .limit(limit).toList();

        if (foundFilms.isEmpty()) {
            throw new NotFoundException("В сервисе не было найдено ни одного фильма с оценками от пользователей");
        }

        return foundFilms;
    }

    @Override
    public ResponseMessage removeLikeFromFilm(Long filmId, Long userId) {
        Film foundFilm = checkFilmOnExistAndGet(filmId);
        checkUserOnExistAndGet(userId);

        Film updatedFilm = getFilmWithRemovedLike(foundFilm, userId);
        films.put(updatedFilm.getId(), updatedFilm);
        log.info("Пользователь {} убрал свой лайк с фильма {}", userId, updatedFilm.getName());
        return new ResponseMessage(String.format("Пользователь с id = %d убрал свой лайк с фильма %s. " +
                "Теперь у фильма '%d' лайков", userId, updatedFilm.getName(), updatedFilm.getUsersLikes().size()));
    }

    @Override
    public ResponseMessage addLikeToFilm(Long filmId, Long userId) {
        Film foundFilm = checkFilmOnExistAndGet(filmId);
        User foundUser = checkUserOnExistAndGet(userId);

        validateUserAlreadyLikedFilm(userId, foundFilm);

        Film updatedFilm = getFilmWithAddedLike(foundFilm, userId);
        films.put(updatedFilm.getId(), updatedFilm);

        log.info("Пользователь {} поставил лайк к фильму {} ", foundUser, foundFilm);
        return new ResponseMessage(String.format(
                "Пользователь name = '%s', id = '%d' поставил лайк к фильму name = '%s', id = '%d'",
                foundUser.getName(), foundUser.getId(), foundFilm.getName(), foundFilm.getId()));
    }

    private static void validateUserAlreadyLikedFilm(Long userId, Film foundFilm) {
        if (foundFilm.getUsersLikes().contains(userId)) {
            throw new NotFoundException(String.format("Пользователь '%d' уже ставил лайк под фильмом '%d' ранее",
                    userId, foundFilm.getId()));
        }
    }

    private User checkUserOnExistAndGet(long userId) throws NotFoundException {
        return userStorage.getUserById(userId).orElseThrow(
                () -> new NotFoundException(String.format("Пользователь с id = '%d' не найден", userId)));
    }

    private Film checkFilmOnExistAndGet(long filmId) throws NotFoundException {
        Film foundFilm = films.get(filmId);
        if (foundFilm == null) {
            throw new NotFoundException(String.format("Фильм с id = '%d' не найден", filmId));
        }
        return foundFilm;
    }

    private Film getFilmWithAddedLike(Film foundFilm, Long userId) {
        return foundFilm.toBuilder()
                .usersLikes(Stream.concat(foundFilm.getUsersLikes().stream(), Stream.of(userId))
                        .collect(Collectors.toSet())).build();
    }

    private Film getFilmWithRemovedLike(Film film, long userId) {
        Set<Long> updatedLikes = film.getUsersLikes();
        if (!updatedLikes.contains(userId)) {
            throw new NotFoundException(String.format("Произошла ошибка во время удаления лайка. " + "Пользователь '%d' не оставлял лайк под постом '%d'", userId, film.getId()));
        }
        updatedLikes.remove(userId);
        return film.toBuilder().usersLikes(updatedLikes).build();
    }

    private long generateUniqueId() {
        long maxId = films.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
        return ++maxId;
    }
}
