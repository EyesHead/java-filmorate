package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.entity.Director;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.entity.Genre;
import ru.yandex.practicum.filmorate.entity.Mpa;
import ru.yandex.practicum.filmorate.repository.mapper.*;

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
        log.debug("(Repo) Выполнение запроса для получения фильма с ID: {}", filmId);

        try {
            Film film = jdbcTemplate.queryForObject(
                    GET_FILMS_BY_ID_QUERY,
                    new FilmRowMapper(),
                    filmId);

            Set<Genre> foundFilmGenres = getFilmGenres(filmId);
            Set<Director> foundFilmDirectors = getFilmDirectors(filmId);

            Film foundFilmWithGenres = film.toBuilder()
                    .genres(foundFilmGenres)
                    .directors(foundFilmDirectors)
                    .build();

            log.debug("(Repo) Фильм с ID {} найден: {}", filmId, foundFilmWithGenres);
            return Optional.ofNullable(foundFilmWithGenres);
        } catch (EmptyResultDataAccessException e) {
            log.debug("(Repo) Фильм с ID {} не найден.", filmId);
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteFilmById(long filmId) {
        log.debug("(Repo) Удаление фильма с id: {}", filmId);
        final String REMOVE_FILM_BY_ID = """
                DELETE
                FROM films
                WHERE film_id = ?
                """;
        boolean isDeleted = jdbcTemplate.update(REMOVE_FILM_BY_ID, filmId) != 0;
        if (isDeleted) {
            log.debug("(Repo) Фильм с id = {} был успешно удалён", filmId);
        } else {
            log.debug("(Repo) Фильм с id = {} не был удалён/Не найден", filmId);
        }
        return isDeleted;
    }

    @Override
    public Collection<Film> getAllFilms() {
        final String GET_ALL_FILMS_QUERY = """
                SELECT f.*, m.mpa_name
                FROM films f
                JOIN mpa m ON f.mpa_id = m.mpa_id
                """;
        log.debug("(Repo) Выполнение запроса для получения всех фильмов");

        List<Film> films = jdbcTemplate.query(
                GET_ALL_FILMS_QUERY,
                new FilmRowMapper());

        assignGenresForFilms(films);
        assignDirectorsForFilms(films);

        log.debug("(Repo) Получено '{}' фильмов.", films.size());
        return films;
    }

    @Override
    public Film saveFilm(Film film) {
        log.debug("(Repo) Сохранение фильма: {}", film.getName());

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
        film = film.toBuilder().id(filmId).build();

        log.debug("(Repo) Фильм с id = '{}' успешно сохранён", filmId);
        return getUpdatedFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        final String UPDATE_FILM_QUERY = """
                UPDATE films
                SET film_name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE film_id = ?
                """;
        log.debug("(Repo) Обновление фильма с ID: {}", film.getId());

        jdbcTemplate.update(UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() == null ? null : film.getMpa().getId(),
                film.getId());

        updateFilmGenres(film.getId(), film.getGenres());
        updateFilmDirectors(film.getId(), film.getDirectors());

        log.debug("(Repo) Фильма с ID: {} был успешно обновлён", film.getId());
        return getUpdatedFilm(film);
    }

    @Override
    public Collection<Film> getFilmsByQuery(String title, String director) {
        final String GET_MOST_LIKED_FILMS_QUERY = """
                SELECT f.*, m.mpa_name
                FROM FILMS f
                LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
                LEFT JOIN users_films_like uf ON f.film_id = uf.film_id
                LEFT JOIN films_directors fd ON fd.film_id  = f.film_id
                LEFT JOIN directors d ON d.id = fd.director_id
                WHERE LOWER (d.name) LIKE '%'|| ? ||'%' OR LOWER(f.FILM_NAME) LIKE '%'|| ? ||'%'
                GROUP BY f.film_id
                ORDER BY COUNT(uf.user_id) DESC
                """;

        log.debug("(Repo) Получение самых популярных фильмов с названием/частью названия: {} {}", title, director);

        LinkedHashSet<Film> films = new LinkedHashSet<>(jdbcTemplate.query(GET_MOST_LIKED_FILMS_QUERY, new FilmRowMapper(), director, title));
        assignGenresForFilms(films);
        assignDirectorsForFilms(films);

        log.debug("(Repo) Получено {} популярных фильмов ", films.size());

        return films;
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

        log.debug("(Repo) Получение самых популярных фильмов с лимитом: {}", limit);

        List<Film> films = jdbcTemplate.query(GET_MOST_LIKED_FILMS_QUERY, new FilmRowMapper(), limit);

        assignGenresForFilms(films);
        assignDirectorsForFilms(films);

        log.debug("(Repo) Получено {} самых популярных фильмов", films.size());
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
        log.debug("(Repo) Получение самых популярных фильмов с лимитом '{}' по жанру id = '{}' и '{}' году", limit, genreId, year);

        LinkedHashSet<Film> films = new LinkedHashSet<>(jdbcTemplate.query(GET_MOST_LIKED_FILMS_QUERY,
                new FilmRowMapper(), genreId, year, limit));

        assignGenresForFilms(films);
        assignDirectorsForFilms(films);

        log.debug("(Repo) Получено {} популярных фильмов по жанру и году", films.size());
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
                WHERE gf.genre_id = ? OR SELECT EXTRACT ( YEAR FROM CAST(f.release_date AS date) ) = ?
                GROUP BY f.film_id, m.mpa_name, gf.genre_id
                ORDER BY COUNT(uf.user_id) DESC
                LIMIT ?
                """;
        log.debug("(Repo) Получение самых популярных фильмов с лимитом {} по жанру '{}', либо году '{}'", limit, genreId, year);

        LinkedHashSet<Film> films = new LinkedHashSet<>(jdbcTemplate.query(GET_MOST_LIKED_FILMS_QUERY,
                new FilmRowMapper(), genreId, year, limit));

        assignGenresForFilms(films);
        assignDirectorsForFilms(films);

        log.debug("(Repo) Получено {} популярных фильмов, отсортированных по {}", films.size(),
                genreId == null ? "году выпуска = " + year : "жанру = " + genreId);
        return films;
    }

    @Override
    public boolean deleteLikeFromFilm(long filmId, long userId) {
        final String DELETE_LIKE_FROM_FILM_QUERY = """
                DELETE
                FROM users_films_like
                WHERE film_id = ? AND user_id = ?
                """;
        log.debug("(Repo) Удаление лайка от пользователя с ID {} для фильма с ID {}", userId, filmId);

        boolean isDeleted = jdbcTemplate.update(DELETE_LIKE_FROM_FILM_QUERY, filmId, userId) != 0;

        log.debug("(Repo) Лайк для фильма с ID {} от пользователя с ID {} {}", filmId, userId, isDeleted ? "удален" : "не был найден");
        return isDeleted;
    }

    @Override
    public boolean saveLikeToFilm(long filmId, long userId) {
        final String INSERT_LIKE_TO_FILM_QUERY = """
                MERGE INTO users_films_like (user_id, film_id)
                VALUES (?, ?)
                """;
        log.debug("(Repo) Добавление лайка пользователя с id = {} к фильму с id = {}", userId, filmId);

        boolean isSaved = jdbcTemplate.update(INSERT_LIKE_TO_FILM_QUERY, userId, filmId) != 0;

        log.debug("(Repo) Лайк к фильму с ID {} от пользователя с ID {} {}", filmId, userId, isSaved ? "добавлен" : "не был добавлен");
        return isSaved;
    }

    @Override
    public Collection<Genre> getAllGenres() {
        final String GET_ALL_GENRES_QUERY = """
                SELECT *
                FROM genres
                ORDER BY genre_id ASC
                """;
        log.debug("(Repo) Получение всех жанров");

        List<Genre> genres = jdbcTemplate.query(GET_ALL_GENRES_QUERY, new GenreRowMapper());
        log.debug("(Repo) Получено {} жанров: {}", genres.size(), genres);
        return genres;
    }

    @Override
    public Optional<Genre> getGenre(long genreId) {
        final String GET_GENRE_BY_ID_QUERY = """
                SELECT *
                FROM genres
                WHERE genre_id = ?
                """;
        log.debug("(Repo) Получение жанра с ID: {}", genreId);

        try {
            Genre genre = jdbcTemplate.queryForObject(
                    GET_GENRE_BY_ID_QUERY,
                    new GenreRowMapper(),
                    genreId
            );
            log.debug("(Repo) Жанр с ID {} найден: {}", genreId, genre);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            log.debug("(Repo) Жанр с ID {} не найден.", genreId);
            return Optional.empty();
        }
    }

    @Override
    public Collection<Mpa> getAllMpa() {
        final String GET_ALL_MPA_RATINGS_QUERY = """
                SELECT *
                FROM mpa
                ORDER BY mpa_id ASC
                """;

        log.debug("(Repo) Получение всех MPA рейтингов из базы данных.");
        Collection<Mpa> mpas = jdbcTemplate.query(GET_ALL_MPA_RATINGS_QUERY, new MpaRowMapper());

        log.debug("(Repo) Получены все MPA рейтинги: {}", mpas);
        return mpas;
    }

    @Override
    public Optional<Mpa> getMpa(long mpaId) {
        final String GET_MPA_RATING_BY_ID = """
            SELECT *
            FROM mpa
            WHERE mpa_id = ?
            """;
        log.debug("(Repo) Получение MPA рейтинга с id = {} из базы данных.", mpaId);

        try {
            Mpa foundMpa = jdbcTemplate.queryForObject(GET_MPA_RATING_BY_ID, new MpaRowMapper(), mpaId);
            log.debug("(Repo) MPA рейтинг с id = {} найден: {}", mpaId, foundMpa);
            return Optional.ofNullable(foundMpa);
        } catch (EmptyResultDataAccessException e) {
            log.debug("(Repo) MPA рейтинг с id = {} не найден в базе данных.", mpaId);
            return Optional.empty();
        }
    }

    private void updateFilmGenres(long filmId, Set<Genre> filmGenres) {
        log.trace("(Repo) Обновление жанров для фильма с ID {}: {}", filmId, filmGenres);

        final String DELETE_FILM_GENRES_QUERY = """
            DELETE
            FROM films_genres
            WHERE film_id = ?
            """;

        jdbcTemplate.update(DELETE_FILM_GENRES_QUERY, filmId);
        log.trace("(Repo) Удалены старые жанры для фильма с ID {}", filmId);

        if (!filmGenres.isEmpty()) {
            final String INSERT_FILM_GENRES_QUERY = """
                INSERT INTO films_genres (film_id, genre_id)
                VALUES (?, ?)
                """;
            for (Genre genre : filmGenres) {
                jdbcTemplate.update(INSERT_FILM_GENRES_QUERY, filmId, genre.getId());
                log.trace("(Repo) В таблицу films_genres добавлена запись с фильмом '{}' и жанром '{}'", filmId, genre.getId());
            }
            log.trace("(Repo) Жанры для фильма в таблице films_genres с ID {} успешно обновлены: {}", filmId, filmGenres);
        }
    }

    private void updateFilmDirectors(long filmId, Set<Director> filmDirectors) {
        log.debug("(Repo) Обновление режиссёров для фильма с ID {}: {}", filmId, filmDirectors);

        final String DELETE_FILM_DIRECTORS_QUERY = """
            DELETE
            FROM films_directors
            WHERE film_id = ?
            """;
        jdbcTemplate.update(DELETE_FILM_DIRECTORS_QUERY, filmId);
        log.trace("(Repo) Удалены старые режиссёры для фильма с ID {}", filmId);

        if (!filmDirectors.isEmpty()) {
            final String INSERT_FILM_DIRECTORS_QUERY = """
                INSERT INTO films_directors (film_id, director_id)
                VALUES (?, ?)
                """;
            for (Director director : filmDirectors) {
                jdbcTemplate.update(INSERT_FILM_DIRECTORS_QUERY, filmId, director.getId());
                log.trace("(Repo) В таблицу films_directors добавлена запись с фильмом '{}' и режиссёром '{}'", filmId, director.getId());
            }
            log.trace("(Repo) Режиссёры в таблице films_directors для фильма с ID {} успешно обновлены: {}", filmId, filmDirectors);
        }
    }

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
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

        log.debug("(Repo) Начало выполнения запроса на получение общих фильмов пользователей с id = {} и id = {}",
                userId, friendId);

        List<Film> commonFilms = jdbcTemplate.query(sql, new FilmRowMapper(), userId, friendId);

        assignGenresForFilms(commonFilms);

        if (commonFilms.isEmpty()) {
            log.debug("(Repo) Общие фильмы для пользователей с id = {} и id = {} не найдены",
                    userId, friendId);
        } else {
            log.debug("(Repo) Общие фильмы для пользователей с id = {} и id = {} успешно получены. Общие фильмы: {}",
                    userId, friendId, commonFilms);
        }

        return commonFilms;
    }

    @Override
    public List<Film> getListOfFilmsById(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            log.debug("(Repo) Возвращен пустой список");
            return Collections.emptyList();
        }
        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        final String GET_LIST_OF_FILMS_BY_ID_QUERY = String.format("""
            SELECT *
            FROM films
            LEFT JOIN mpa ON mpa.mpa_id = films.mpa_id
            WHERE films.film_id IN (%s)
            """, inSql);
        log.debug("(Repo) Список фильмов получен");
        List<Film> extractingFilms = jdbcTemplate.query(GET_LIST_OF_FILMS_BY_ID_QUERY, new FilmRowMapper(), filmIds.toArray());
        log.debug("(Repo) Фильмы: {}", extractingFilms);
        assignGenresForFilms(extractingFilms);
        assignDirectorsForFilms(extractingFilms);
        return extractingFilms;
    }

    @Override
    public List<Film> getSortedByReleaseDateFilmsOfDirector(long directorId) {
        final String GET_SORTED_FILMS_BY_DIRECTOR_QUERY = """
            SELECT f.*, m.mpa_name
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
            LEFT JOIN films_directors fd ON f.film_id = fd.film_id
            WHERE fd.director_id = ?
            ORDER BY EXTRACT(YEAR FROM CAST(f.release_date AS date)) ASC
            """;

        log.debug("(Repo) Получение фильмов режиссёра с id = '{}' отсортированных по году выпуска", directorId);

        List<Film> films = jdbcTemplate.query(GET_SORTED_FILMS_BY_DIRECTOR_QUERY, new FilmRowMapper(), directorId);

        assignGenresForFilms(films);
        assignDirectorsForFilms(films);

        log.debug("(Repo) Получено {} фильмов режиссёра отсортированных по году выпуска", films.size());
        return films;
    }

    @Override
    public List<Film> getSortedByLikesFilmsOfDirector(long directorId) {
        final String GET_SORTED_FILMS_BY_DIRECTOR_QUERY = """
                SELECT f.*, m.mpa_name, COUNT(uf.user_id) AS likes_count
                FROM films f
                LEFT JOIN users_films_like uf ON f.film_id = uf.film_id
                LEFT JOIN films_directors fd ON f.film_id = fd.film_id
                LEFT JOIN mpa m ON m.mpa_id = f.mpa_id
                WHERE fd.director_id = ?
                GROUP BY f.film_id
                ORDER BY likes_count DESC
                """;

        log.debug("(Repo) Получение фильмов режиссёра с id = '{}' отсортированных по количеству лайков", directorId);

        List<Film> films = jdbcTemplate.query(GET_SORTED_FILMS_BY_DIRECTOR_QUERY,
                new FilmRowMapper(), directorId);

        assignGenresForFilms(films);
        assignDirectorsForFilms(films);

        log.debug("(Repo) Получено {} фильмов режиссёра отсортированных по количеству лайков", films.size());
        return films;
    }

    /**
     * Метод обновляет данные во множестве фильмов, переданных в качестве аргумента. А именно,
     * устанавливает для каждого фильма из множества соответствующие ему жанры
     *
     * @param films множество фильмов, для которых нужно установить соответствующие жанры
     */
    private void assignGenresForFilms(Collection<Film> films) {
        log.trace("(Repo) Начало добавления жанров к полученным фильмам. films = {}", films);
        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .toList();

        if (filmIds.isEmpty()) {
            log.trace("(Repo) Список ID фильмов пуст. Жанры не будут назначены переданным фильмам.");
            return;
        }

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sqlQuery = String.format("""
            SELECT fg.film_id, g.genre_id, g.name
            FROM films_genres fg
            JOIN genres g ON fg.genre_id = g.genre_id
            WHERE fg.film_id IN (%s)""", inSql);

        Map<Long, Set<Genre>> filmGenresMap = jdbcTemplate.query(sqlQuery, filmIds.toArray(), new FilmGenresRowMapper());
        log.trace("(Repo) Получена хэш-карта, где key = id фильма, value = все id жанров данного фильма. " +
                "Map.size = {}", filmGenresMap.size());

        for (Film film : films) {
            Set<Genre> genres = filmGenresMap.getOrDefault(film.getId(), new HashSet<>());
            film.getGenres().addAll(genres);
            log.trace("(Repo) Для фильма с id = {}, было добавлено {} жанров, найденных в бд",
                    film.getId(), genres.size());
        }

        log.trace("(Repo) Жанры для size='{}' фильмов были успешно добавлены", films.size());
    }


    /**
     * Метод обновляет данные во множестве фильмов, переданных в качестве аргумента. А именно,
     * устанавливает для каждого фильма из множества соответствующих ему режиссёров
     *
     * @param films множество фильмов, для которых нужно установить соответствующих режиссёров
     */
    private void assignDirectorsForFilms(Collection<Film> films) {
        log.trace("(Repo) Начало добавления режиссёров к полученным фильмам. films = {}", films);
        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .toList();

        if (filmIds.isEmpty()) {
            log.trace("(Repo) Список ID фильмов пуст. Режиссёры не будут назначены переданным фильмам.");
            return;
        }
        String filmIdsSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sqlQuery = String.format("""
                SELECT fd.film_id, d.id AS director_id, d.name
                FROM films_directors fd
                JOIN directors d ON fd.director_id = d.id
                WHERE fd.film_id IN (%s)""", filmIdsSql);
        Map<Long, Set<Director>> filmDirectorsMap = jdbcTemplate.query(
                sqlQuery,
                filmIds.toArray(),
                new FilmDirectorsRowMapper());
        log.trace("(Repo) Получена хэш-карта, где key = id фильма, value = все id режиссёров данного фильма . " +
                "Map.size = {}", filmDirectorsMap.size());

        for (Film film : films) {
            Set<Director> directors = filmDirectorsMap.getOrDefault(film.getId(), new HashSet<>());
            film.getDirectors().addAll(directors);
            log.trace("(Repo) Для фильма с id = {}, было добавлено {} режиссёров, найденых в бд",
                    film.getId(), directors.size());
        }
        log.trace("(Repo) Режиссёры для size='{}' переданных фильмов были успешно добавлены", films.size());
    }


    private Set<Genre> getFilmGenres(long filmId) {
        final String GET_GENRES_BY_FILM_ID_QUERY = """
                SELECT g.genre_id, g.name
                FROM genres g
                INNER JOIN films_genres fg ON g.genre_id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.genre_id ASC""";
        log.trace("(Repo) Получение жанров для фильма с id = {}", filmId);

        Set<Genre> genres = new LinkedHashSet<>(jdbcTemplate.query(
                GET_GENRES_BY_FILM_ID_QUERY, new GenreRowMapper(), filmId));
        log.trace("(Repo) Жанры для фильма с id найдены: filmId = {}, genres = {}", filmId, genres);
        return genres;
    }

    private Set<Director> getFilmDirectors(long filmId) {
        final String GET_DIRECTORS_BY_FILM_ID_QUERY = """
                SELECT d.id, d.name
                FROM directors d
                INNER JOIN films_directors fd ON d.id = fd.director_id
                WHERE fd.film_id = ?
                ORDER BY d.id ASC""";
        log.trace("(Repo) Получение режиссёров для фильма с id = {}", filmId);

        Set<Director> directors = new LinkedHashSet<>(jdbcTemplate.query(
                GET_DIRECTORS_BY_FILM_ID_QUERY, new DirectorRowMapper(), filmId));
        log.trace("(Repo) Режиссёры для фильма с id найдены: filmId = {}, directors.size() = {}", filmId, directors.size());
        return directors;
    }

    private Optional<Mpa> getFilmMpa(long filmId) {
        final String GET_MPA_BY_FILM_ID_QUERY = """
                SELECT * FROM mpa m
                INNER JOIN films f ON m.mpa_id = f.mpa_id
                WHERE f.film_id = ?""";
        log.trace("(Repo) Выполнение запроса для получения MPA фильма с ID: {}", filmId);

        try {
            Mpa mpa = jdbcTemplate.queryForObject(
                    GET_MPA_BY_FILM_ID_QUERY,
                    new MpaRowMapper(),
                    filmId);
            log.trace("(Repo) MPA для фильма с ID {} найдено: {}", filmId, mpa);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            log.trace("(Repo) MPA для фильма с ID {} не найдено.", filmId);
            return Optional.empty();
        }
    }

    private Film getUpdatedFilm(Film film) {
        long filmId = film.getId();

        Mpa filmMpa = null;
        if (film.getMpa() != null) {
            filmMpa = getFilmMpa(filmId).orElse(null);
            log.trace("(Repo) У фильма с id = {} был обновлён Mpa рейтинг {}", filmId, filmMpa);
        }
        Set<Genre> filmGenres = new HashSet<>();
        if (CollectionUtils.isNotEmpty(film.getGenres())) {
            updateFilmGenres(filmId, film.getGenres());
            filmGenres = getFilmGenres(filmId);
            log.trace("(Repo) Жанры для фильма с id ='{}' успешно обновлены. size = {}", filmId, filmGenres.size());
        }
        Set<Director> filmDirectors = new HashSet<>();
        if (CollectionUtils.isNotEmpty(film.getDirectors())) {
            updateFilmDirectors(filmId, film.getDirectors());
            filmDirectors = getFilmDirectors(filmId);
            log.trace("(Repo) Режиссёры для фильма с id ='{}' успешно обновлены. size = {}", filmId, filmDirectors.size());
        }
        return film.toBuilder()
                .genres(filmGenres)
                .directors(filmDirectors)
                .mpa(filmMpa)
                .build();
    }
}
