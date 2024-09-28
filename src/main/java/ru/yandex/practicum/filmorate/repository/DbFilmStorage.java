package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.entity.Mpa;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.entity.Genre;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
        log.debug("Выполнение запроса для получения фильма с ID: {}", filmId);

        try {
            Film film = jdbcTemplate.queryForObject(
                    GET_FILMS_BY_ID_QUERY,
                    new SimpleFilmRowMapper(),
                    filmId);

            Set<Genre> foundFilmGenres = getFilmGenres(filmId);

            Film foundFilmWithGenres = film.toBuilder()
                    .genres(foundFilmGenres)
                    .build();

            log.debug("Фильм с ID {} найден: {}", filmId, foundFilmWithGenres);
            return Optional.ofNullable(foundFilmWithGenres);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Фильм с ID {} не найден.", filmId);
            return Optional.empty();
        }
    }

    private Optional<Mpa> getFilmMpa(long filmId) {
        final String GET_MPA_BY_FILM_ID_QUERY = """
                SELECT * FROM mpa m
                INNER JOIN films f ON m.mpa_id = f.mpa_id
                WHERE f.film_id = ?""";
        log.debug("Выполнение запроса для получения MPA фильма с ID: {}", filmId);

        try {
            Mpa mpa = jdbcTemplate.queryForObject(
                    GET_MPA_BY_FILM_ID_QUERY,
                    new MpaRowMapper(),
                    filmId);
            log.debug("MPA для фильма с ID {} найдено: {}", filmId, mpa);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            log.warn("MPA для фильма с ID {} не найдено.", filmId);
            return Optional.empty();
        }
    }

    @Override
    public Collection<Film> getAllFilms() {
        Map<Long, Set<Genre>> filmGenresMap = getAllFilmIdsWithGenres();

        final String GET_ALL_FILMS_QUERY = """
                SELECT f.*, m.mpa_name
                FROM films f
                JOIN mpa m ON f.mpa_id = m.mpa_id
                """;
        log.debug("Выполнение запроса для получения всех фильмов");

        List<Film> films = jdbcTemplate.query(
                GET_ALL_FILMS_QUERY,
                new FilmGenresRowMapper(filmGenresMap));

        log.debug("Получено {} фильмов.", films.size());
        return films;
    }

    private Map<Long, Set<Genre>> getAllFilmIdsWithGenres() {
        final String GET_ALL_FILM_GENRES_QUERY = """
                SELECT fg.film_id, g.genre_id, g.name
                FROM films_genres fg
                INNER JOIN genres g ON fg.genre_id = g.genre_id
                """;
        log.debug("Получение жанров для всех фильмов");

        Map<Long, Set<Genre>> filmGenresMap = new HashMap<>();
        jdbcTemplate.query(GET_ALL_FILM_GENRES_QUERY, rs -> {
            long filmId = rs.getLong("film_id");
            Genre genre = new Genre(
                    rs.getLong("genre_id"),
                    rs.getString("name")
            );
            filmGenresMap.computeIfAbsent(filmId, id -> new HashSet<>()).add(genre);
        });

        log.debug("Жанры получены для {} фильмов", filmGenresMap.size());
        return filmGenresMap;
    }

    @Override
    public Film saveFilm(Film film) {
        log.info("Сохранение фильма: {}", film.getName());

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
        log.debug("Фильм добавлен с ID: {}", filmId);

        // Получаем MPA рейтинг фильма с названием (если был указан)
        Mpa filmMpa = null;
        if (film.getMpa() != null) {
            filmMpa = getFilmMpa(filmId).orElse(null);
        }

        // Получаем жанры фильмов с названиями (если были указаны)
        Set<Genre> filmGenres = new HashSet<>();
        if (CollectionUtils.isNotEmpty(film.getGenres())) {
            updateFilmGenres(filmId, film.getGenres());
            filmGenres = getFilmGenres(filmId);
        }

        Film savedFilm = film.toBuilder()
                .id(filmId)
                .mpa(filmMpa)
                .genres(filmGenres)
                .build();

        log.info("Фильм успешно сохранён: {}", savedFilm);
        return savedFilm;
    }

    @Override
    public Film updateFilm(Film film) {
        final String UPDATE_FILM_QUERY = """
                UPDATE films
                SET film_name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE film_id = ?
                """;
        log.info("Обновление фильма с ID: {}", film.getId());

        jdbcTemplate.update(UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() == null ? null : film.getMpa().getId(),
                film.getId());

        Film updatedFilm = getUpdatedFilm(film);

        log.info("Фильм обновлен: {}", updatedFilm);
        return updatedFilm;
    }

    private Film getUpdatedFilm(Film film) {
        var filmBuilder = film.toBuilder();

        long filmId = film.getId();
        Set<Genre> filmGenres = film.getGenres();
        if (CollectionUtils.isNotEmpty(filmGenres)) {
            updateFilmGenres(filmId, filmGenres);
            filmBuilder.genres(getFilmGenres(filmId));
        }

        return filmBuilder
                .mpa(getFilmMpa(filmId).orElse(null))
                .build();
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

        log.debug("Получение самых популярных фильмов с лимитом: {}", limit);

        Map<Long, Set<Genre>> filmGenresMap = getAllFilmIdsWithGenres();

        List<Film> films = jdbcTemplate.query(GET_MOST_LIKED_FILMS_QUERY,
                new FilmGenresRowMapper(filmGenresMap),
                limit);

        log.debug("Получено {} популярных фильмов", films.size());
        return films;
    }

    @Override
    public Collection<Film> getMostLikedFilmsByGenreAndYear(int limit, int genreId, int year) {
        final String GET_MOST_LIKED_FILMS_QUERY = """
                SELECT f.*, m.mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
                LEFT JOIN users_films_like uf ON f.film_id = uf.film_id
                LEFT JOIN films_genres gf ON gf.film_id = f.film_id
                WHERE gf.genre_id = ? AND SELECT EXTRACT (YEAR FROM CAST(f.release_date AS date) ) = ?
                GROUP BY f.film_id, m.mpa_name, gf.genre_id
                ORDER BY COUNT(uf.user_id) DESC
                LIMIT ?
                """;
        log.debug("Получение самых популярных фильмов с лимитом: {}", limit);

        Map<Long, Set<Genre>> filmGenresMap = getAllFilmIdsWithGenres();

        List<Film> films = jdbcTemplate.query(GET_MOST_LIKED_FILMS_QUERY,
                new FilmGenresRowMapper(filmGenresMap), genreId, year, limit);

        log.debug("Получено {} популярных фильмов", films.size());
        return films;
    }

    @Override
    public Collection<Film> getMostLikedFilmsByGenreOrYear(Integer limit, Integer genreId, Integer year) {
        final String GET_MOST_LIKED_FILMS_QUERY = """
                SELECT f.*, m.mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
                LEFT JOIN users_films_like uf ON f.film_id = uf.film_id
                LEFT JOIN films_genres gf ON gf.film_id = f.film_id
                WHERE gf.genre_id = ? OR SELECT EXTRACT (YEAR FROM CAST(f.release_date AS date) ) = ?
                GROUP BY f.film_id, m.mpa_name, gf.genre_id
                ORDER BY COUNT(uf.user_id) DESC
                LIMIT ?
                """;
        log.debug("Получение самых популярных фильмов с лимитом: {}", limit);

        Map<Long, Set<Genre>> filmGenresMap = getAllFilmIdsWithGenres();

        List<Film> films = jdbcTemplate.query(GET_MOST_LIKED_FILMS_QUERY,
                new FilmGenresRowMapper(filmGenresMap), genreId, year, limit);

        log.debug("Получено {} популярных фильмов", films.size());
        return films;
    }

    @Override
    public boolean deleteLikeFromFilm(long filmId, long userId) {
        final String DELETE_LIKE_FROM_FILM_QUERY = "DELETE FROM users_films_like WHERE film_id = ? AND user_id = ?";
        log.debug("Удаление лайка от пользователя с ID {} для фильма с ID {}", userId, filmId);

        int rowsDeleted = jdbcTemplate.update(DELETE_LIKE_FROM_FILM_QUERY, filmId, userId);
        boolean isDeleted = rowsDeleted != 0;

        log.info("Лайк для фильма с ID {} от пользователя с ID {} {}", filmId, userId, isDeleted ? "удален" : "не был найден");
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
    public Collection<Genre> getAllGenres() {
        final String GET_ALL_GENRES_QUERY = "SELECT * FROM genres ORDER BY genre_id ASC";
        log.debug("Получение всех жанров");

        List<Genre> genres = jdbcTemplate.query(GET_ALL_GENRES_QUERY, new GenreRowMapper());
        log.debug("Получено {} жанров: {}", genres.size(), genres);
        return genres;
    }

    @Override
    public Optional<Genre> getGenre(long genreId) {
        final String GET_GENRE_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
        log.debug("Получение жанра с ID: {}", genreId);

        try {
            Genre genre = jdbcTemplate.queryForObject(
                    GET_GENRE_BY_ID_QUERY,
                    new GenreRowMapper(),
                    genreId
            );
            log.debug("Жанр с ID {} найден: {}", genreId, genre);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Жанр с ID {} не найден.", genreId);
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
        log.debug("Обновление жанров для фильма с ID {}: {}", filmId, filmGenres);
        // Удаление существующих жанров фильма
        final String DELETE_FILM_GENRES_QUERY = "DELETE FROM films_genres WHERE film_id = ?";
        jdbcTemplate.update(DELETE_FILM_GENRES_QUERY, filmId);
        log.debug("Удалены старые жанры для фильма с ID {}", filmId);

        // Вставка новых жанров для фильма
        final String INSERT_FILM_GENRES_QUERY = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : filmGenres) {
            jdbcTemplate.update(INSERT_FILM_GENRES_QUERY, filmId, genre.getId());
            log.trace("В таблицу films_genres добавлена запись с фильмом '{}' и жанром '{}'", filmId, genre);
        }
        log.info("Жанры для фильма с ID {} успешно обновлены: {}", filmId, filmGenres);
    }

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
        Map<Long, Set<Genre>> filmGenresMap = getAllFilmIdsWithGenres();

        String sql = """
                    SELECT f.*, mpa.*, COUNT(uf.user_id) AS likes_count
                    FROM films AS f
                    LEFT JOIN mpa ON mpa.mpa_id = f.mpa_id
                    LEFT JOIN users_films_like AS uf ON f.film_id = uf.film_id
                    WHERE f.film_id IN (
                        SELECT l1.film_id
                        FROM users_films_like AS l1
                        JOIN users_films_like AS l2 ON l1.film_id = l2.film_id
                        WHERE l1.user_id = ? AND l2.user_id = ?
                    )
                    GROUP BY f.film_id
                    ORDER BY likes_count DESC
                """;

        log.debug("Начало выполнения запроса на получение общих фильмов пользователей с id = {} и id = {}. Запрос: {}",
                userId, friendId, sql);

        List<Film> commonFilms = jdbcTemplate.query(sql, new FilmGenresRowMapper(filmGenresMap), userId, friendId);

        if (commonFilms.isEmpty()) {
            log.info("Общие фильмы для пользователей с id = {} и id = {} не найдены.", userId, friendId);
        } else {
            log.info("Общие фильмы для пользователей с id = {} и id = {} успешно получены. Общие фильмы: {}",
                    userId, friendId, commonFilms);
        }

        return commonFilms;
    }

    @Override
    public void deleteFilmById(long filmId) {
        final String sql = "DELETE FROM films WHERE film_id = ?";
        int status = jdbcTemplate.update(sql, filmId);
        if (status == 0) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        } else {
            log.info("Фильм с id = {} успешно удален. ", filmId);
        }
    }
}
