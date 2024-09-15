package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.entity.Mpa;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.entity.Genre;
import ru.yandex.practicum.filmorate.repository.mapper.FilmGenresRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.SimpleFilmRowMapper;

import java.util.*;

@RequiredArgsConstructor
@Repository
@Slf4j
public class DbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Film> getFilmById(long filmId) {
        final String GET_FILMS_BY_ID_QUERY = """
                SELECT f.*, m.mpa_name
                FROM films f
                JOIN mpa m ON m.mpa_id = f.mpa_id
                WHERE film_id = ?
                """;
        log.debug("Получение фильма с id = {} из БД. Запрос = '{}'", filmId, GET_FILMS_BY_ID_QUERY);

        try {
            Film film = jdbcTemplate.queryForObject(
                    GET_FILMS_BY_ID_QUERY,
                    new SimpleFilmRowMapper(),
                    filmId);

            Set<Genre> foundFilmGenres = getFilmGenres(filmId); // получаем жанры для фильма с названиями

            Film foundFilmWithGenres = film.toBuilder()
                    .genres(foundFilmGenres)
                    .build();

            log.debug("Фильм найден. {}", foundFilmWithGenres);
            return Optional.ofNullable(foundFilmWithGenres);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Фильм с id = {} не найден в базе данных.", filmId);
            return Optional.empty();
        }
    }

    private Optional<Mpa> getFilmMpa(long filmId) {
        final String GET_MPA_BY_FILM_ID_QUERY = """
                SELECT * FROM mpa m
                INNER JOIN films f ON m.mpa_id = f.mpa_id
                WHERE f.film_id = ?""";
        log.debug("Получение mpa для фильма с id = {} из БД. Запрос = '{}'", filmId, GET_MPA_BY_FILM_ID_QUERY);

        try {
            Mpa mpa = jdbcTemplate.queryForObject(
                    GET_MPA_BY_FILM_ID_QUERY,
                    new MpaRowMapper(),
                    filmId);
            log.debug("MPA для фильма с id = {} найдено: {}", filmId, mpa);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            log.warn("MPA для фильма с id = {} не найдено.", filmId);
            return Optional.empty();
        }
    }

    @Override
    public Collection<Film> getAllFilms() {
        log.debug("Получение всех фильмов из базы данных.");

        Map<Long, Set<Genre>> filmGenresMap = getAllFilmIdsWithGenres();

        final String GET_ALL_FILMS_QUERY = """
                SELECT f.*, m.mpa_name
                FROM films f
                JOIN mpa m ON f.mpa_id = m.mpa_id
                """;
        List<Film> films = jdbcTemplate.query(
                GET_ALL_FILMS_QUERY,
                new FilmGenresRowMapper(filmGenresMap));

        log.debug("Получены все фильмы: {}", films);
        return films;
    }

    private Map<Long, Set<Genre>> getAllFilmIdsWithGenres() {
        final String GET_ALL_FILM_GENRES_QUERY = """
                SELECT fg.film_id, g.genre_id, g.name
                FROM films_genres fg
                INNER JOIN genres g ON fg.genre_id = g.genre_id
                """;

        Map<Long, Set<Genre>> filmGenresMap = new HashMap<>();
        jdbcTemplate.query(GET_ALL_FILM_GENRES_QUERY, rs -> {
            long filmId = rs.getLong("film_id");
            Genre genre = new Genre(
                    rs.getLong("genre_id"),
                    rs.getString("name")
            );
            filmGenresMap.computeIfAbsent(filmId, id -> new HashSet<>()).add(genre);
        });
        log.debug("Получены жанры для фильмов: {}", filmGenresMap);
        return filmGenresMap;
    }

    @Override
    public Film saveFilm(Film film) {
        log.debug("Сохранение нового фильма в базу данных. {}", film);
        // шаг 1. добавление через SimpleJdbcInsert фильма
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("film_name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("release_date", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("mpa_id", film.getMpa() == null ? null : film.getMpa().getId());

        long filmId = insertActor.executeAndReturnKey(parameters).longValue();
        log.debug("Фильм успешно добавлен с id = {}", filmId);

        Mpa mpa = getFilmMpa(filmId).orElse(null);

        // Шаг 2. Обновление жанров фильма в таблице films_genres (если они указаны)
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateFilmGenres(filmId, film.getGenres());
        }

        Film savedFilm = film.toBuilder()
                .id(filmId)
                .mpa(mpa)
                .genres(getFilmGenres(filmId))
                .build();

        log.info("Фильм успешно создан и записан в базу данных. {}", savedFilm);
        return savedFilm;
    }

    @Override
    public Film updateFilm(Film film) {
        final String UPDATE_FILM_QUERY = """
                UPDATE films
                SET film_name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE film_id = ?
                """;
        log.debug("Обновление фильма в базе данных. {}", film);
        jdbcTemplate.update(UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() == null ? null : film.getMpa().getId(),
                film.getId());

        var filmBuilder = film.toBuilder();

        Set<Genre> filmGenres = film.getGenres();
        long filmId = film.getId();

        // Вставляем новые жанры для фильма (если они указаны)
        if (filmGenres != null && !filmGenres.isEmpty()) {
            updateFilmGenres(filmId, filmGenres);
            filmBuilder.genres(getFilmGenres(filmId));
        }

        Film updatedFilm = filmBuilder
                .mpa(getFilmMpa(filmId).orElse(null))
                .build();
        log.info("Фильм успешно обновлен в БД. {}", updatedFilm);
        return updatedFilm;
    }

    @Override
    public Collection<Film> getMostLikedFilms(int limit) {
        final String GET_MOST_LIKED_FILMS_QUERY = """
                SELECT f.*, m.mpa_name, COUNT(uf.user_id) AS likes_count
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
                LEFT JOIN users_films_like uf ON f.film_id = uf.film_id
                GROUP BY f.film_id, m.mpa_name
                ORDER BY likes_count DESC
                LIMIT ?
                """;

        log.debug("Получение самых популярных фильмов с лимитом = {}", limit);

        // Получаем жанры для фильмов
        Map<Long, Set<Genre>> filmGenresMap = getAllFilmIdsWithGenres();

        // Запрос для получения фильмов
        List<Film> films = jdbcTemplate.query(GET_MOST_LIKED_FILMS_QUERY,
                new FilmGenresRowMapper(filmGenresMap),
                limit);

        log.debug("Получены самые популярные фильмы: {}", films);
        return films;
    }

    @Override
    public boolean deleteLikeFromFilm(long filmId, long userId) {
        final String DELETE_LIKE_FROM_FILM_QUERY = "DELETE FROM users_films_like WHERE film_id = ? AND user_id = ?";
        log.debug("Удаление лайка пользователя с id = {} от фильма с id = {}", userId, filmId);

        int rowsDeleted = jdbcTemplate.update(DELETE_LIKE_FROM_FILM_QUERY, filmId, userId);
        boolean isDeleted = rowsDeleted != 0;

        log.info("Лайк для фильма с id = {} от пользователя с id = {} {} удален.", filmId, userId, isDeleted ? "успешно" : "не был найден");
        return isDeleted;
    }

    @Override
    public void saveLikeToFilm(long filmId, long userId) {
        final String INSERT_LIKE_TO_FILM_QUERY = "INSERT INTO users_films_like (user_id, film_id) VALUES (?, ?)";
        log.debug("Добавление лайка пользователя с id = {} к фильму с id = {}", userId, filmId);

        jdbcTemplate.update(INSERT_LIKE_TO_FILM_QUERY, userId, filmId);
        log.info("Лайк для фильма с id = {} от пользователя с id = {} успешно добавлен.", filmId, userId);
    }

    @Override
    public List<Long> getUsersIdsWhoLikedFilm(long filmId) {
        final String GET_USERS_IDS_WHO_LIKED_FILM_QUERY = "SELECT user_id FROM users_films_like WHERE film_id = ?";
        log.debug("Получение id пользователей, которые поставили лайк фильму с id = {}", filmId);

        List<Long> userIds = jdbcTemplate.query(GET_USERS_IDS_WHO_LIKED_FILM_QUERY,
                (rs, rowNum) -> rs.getLong("user_id"), filmId);

        log.debug("Полученные id пользователей, которые поставили лайк фильму с id = {}: {}", filmId, userIds);
        return userIds;
    }

    @Override
    public List<Genre> getAllGenres() {
        final String GET_ALL_GENRES_QUERY = "SELECT * FROM genres ORDER BY genre_id ASC";

        log.debug("Получение всех жанров из базы данных.");
        List<Genre> genres = jdbcTemplate.query(GET_ALL_GENRES_QUERY, new GenreRowMapper());

        log.debug("Получены все жанры: {}", genres);
        return genres;
    }

    @Override
    public Optional<Genre> getGenre(long genreId) {
        final String GET_GENRE_BY_ID = "SELECT * FROM genres WHERE genre_id = ?";
        log.debug("Получение жанра с id = {} из базы данных.", genreId);

        try {
            Genre foundGenre = jdbcTemplate.queryForObject(GET_GENRE_BY_ID, new GenreRowMapper(), genreId);
            log.debug("Жанр с id = {} найден: {}", genreId, foundGenre);
            return Optional.ofNullable(foundGenre);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Жанр с id = {} не найден в базе данных.", genreId);
            return Optional.empty();
        }
    }

    @Override
    public Set<Genre> getFilmGenres(long filmId) {
        final String GET_GENRES_BY_FILM_ID_QUERY = """
            SELECT g.genre_id, g.name
            FROM genres g
            INNER JOIN films_genres fg ON g.genre_id = fg.genre_id
            WHERE fg.film_id = ?
            ORDER BY g.genre_id ASC""";
        log.debug("Получение жанров для фильма с id = {}", filmId);

        // Используем LinkedHashSet для сохранения порядка жанров
        Set<Genre> genres = new LinkedHashSet<>(jdbcTemplate.query(GET_GENRES_BY_FILM_ID_QUERY, new GenreRowMapper(), filmId));
        log.debug("Жанры для фильма с id = {}: {}", filmId, genres);
        return genres;
    }

    @Override
    public Collection<Mpa> getAllMpa() {
        final String GET_ALL_MPA_RATINGS_QUERY = "SELECT * FROM mpa ORDER BY mpa_id ASC";

        log.debug("Получение всех MPA рейтингов из базы данных.");
        Collection<Mpa> mpas = jdbcTemplate.query(GET_ALL_MPA_RATINGS_QUERY, new MpaRowMapper());

        log.debug("Получены все MPA рейтинги: {}", mpas);
        return mpas;
    }

    @Override
    public Optional<Mpa> getMpa(long mpaId) {
        final String GET_MPA_RATING_BY_ID = "SELECT * FROM mpa WHERE mpa_id = ?";
        log.debug("Получение MPA рейтинга с id = {} из базы данных.", mpaId);

        try {
            Mpa foundMpa = jdbcTemplate.queryForObject(GET_MPA_RATING_BY_ID, new MpaRowMapper(), mpaId);
            log.debug("MPA рейтинг с id = {} найден: {}", mpaId, foundMpa);
            return Optional.ofNullable(foundMpa);
        } catch (EmptyResultDataAccessException e) {
            log.warn("MPA рейтинг с id = {} не найден в базе данных.", mpaId);
            return Optional.empty();
        }
    }

    private void updateFilmGenres(long filmId, Set<Genre> filmGenres) {
        log.debug("Обновление таблицы films_genres для фильма с id = {}", filmId);
        // Удаление существующих жанров фильма
        final String DELETE_FILM_GENRES_QUERY = "DELETE FROM films_genres WHERE film_id = ?";
        jdbcTemplate.update(DELETE_FILM_GENRES_QUERY, filmId);
        log.debug("Удалены все жанры для фильма с id = {}", filmId);

        // Вставка новых жанров для фильма
        final String INSERT_FILM_GENRES_QUERY = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : filmGenres) {
            jdbcTemplate.update(INSERT_FILM_GENRES_QUERY, filmId, genre.getId());
            log.trace("В таблицу films_genres добавлена запись с фильмом '{}' и жанром '{}'", filmId, genre);
        }
        log.info("Таблица films_genres успешно обновлена для фильма с id = {}", filmId);
    }
}
