package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.responses.success.ResponseMessage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.dto.Film;

import java.util.*;

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
                .usersLikes(new HashSet<>())
                .build();

        films.put(newFilm.getId(), newFilm);
        log.trace("Фильм успешно создан и добавлен : {}", newFilm);
        log.info("Фильм \"{}\" был успешно создан и добавлен в репозиторий", newFilm.getName());
        return newFilm;
    }

    @Override
    public Film updateFilm(Film film) throws NotFoundException {
        validateFilmExists(film.getId());

        Film updatedFilm = film.toBuilder()
                .usersLikes(films.get(film.getId()).getUsersLikes())
                .build();

        films.put(updatedFilm.getId(), updatedFilm);
        log.debug("Фильм был успешно найден и обновлён: {}", updatedFilm);
        log.info("Данные о фильме с id =\"{}\" успешно обновлены", updatedFilm.getId());
        return updatedFilm;
    }

    @Override
    public Collection<Film> getMostLikedFilms(int limit) {
        if (films.isEmpty()) {
            throw new NotFoundException("Пока что в сервис не было добавлено ни одного фильма");
        }

        return films.values().stream()
                .filter(film -> film.getUsersLikes() != null && !film.getUsersLikes().isEmpty())
                .sorted(Comparator.comparingInt(film -> ((Film) film).getUsersLikes().size()).reversed())
                .limit(limit).toList();
    }

    @Override
    public ResponseMessage removeLikeFromFilm(long filmId, long userId) {
        validateFilmExists(filmId);
        validateUserExists(userId);

        Film updatedFilm = getFilmWithRemovedLike(films.get(filmId), userId);
        films.put(updatedFilm.getId(), updatedFilm);
        log.info("Пользователь {} убрал свой лайк с фильма {}", userId, updatedFilm.getName());
        return new ResponseMessage(String.format("Пользователь с id = %d убрал свой лайк с фильма %s. " +
                "Теперь у фильма '%d' лайков", userId, updatedFilm.getName(), updatedFilm.getUsersLikes().size()));
    }

    @Override
    public ResponseMessage addLikeToFilm(long filmId, long userId) {
        validateFilmExists(filmId);
        validateUserExists(userId);

        Film foundFilm = films.get(filmId);
        if (!foundFilm.getUsersLikes().add(userId)) {
            throw new NotFoundException(String.format("Пользователь '%d' уже ставил лайк под фильмом '%d' ранее", userId, filmId));
        }
        log.info("Пользователь с id = '{}' поставил лайк к фильму {}", userId, foundFilm.getName());
        return new ResponseMessage(String.format("Пользователь с id = '%d' поставил лайк к фильму %s c id = %d",
                userId, foundFilm.getName(), foundFilm.getId()));
    }

    private void validateUserExists(long userId) throws NotFoundException {
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id = '%d' не найден", userId));
        }
    }

    private void validateFilmExists(long filmId) throws NotFoundException {
        if (films.get(filmId) == null) {
            throw new NotFoundException(String.format("Фильм с id = '%d' не найден", filmId));
        }
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
