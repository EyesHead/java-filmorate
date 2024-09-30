package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.entity.Director;
import ru.yandex.practicum.filmorate.repository.mapper.DirectorRowMapper;

import java.util.*;

@RequiredArgsConstructor
@Repository
@Slf4j
public class DbDirectorStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Director> getDirectorById(long directorId) {
        final String GET_DIRECTOR_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
        log.debug("Начало выполнения запроса для получения режиссера с ID: {}", directorId);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(GET_DIRECTOR_BY_ID_QUERY,
                    new DirectorRowMapper(),
                    directorId));
        } catch (EmptyResultDataAccessException e) {
            log.warn("Режиссёр с ID {} не найден.", directorId);
            return Optional.empty();
        }
    }

    @Override
    public Director saveDirector(Director director) {
        log.info("Начало сохранения режиссера '{}' в БД", director.getName());

        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("id");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", director.getName());

        long directorId = insertActor.executeAndReturnKey(parameters).longValue();

        Director savedDirector = director.toBuilder()
                .id(directorId)
                .build();

        log.info("Режиссер успешно сохранён в БД: {}", savedDirector);
        return savedDirector;
    }

    @Override
    public List<Director> getAllDirectors() {
        final String GET_ALL_DIRECTORS_QUERY = "SELECT * FROM directors ORDER BY id ASC";
        log.debug("Начало получения всех режиссеров в БД");

        List<Director> directors = jdbcTemplate.query(GET_ALL_DIRECTORS_QUERY, new DirectorRowMapper());
        log.info("Получено {} жанров из БД", directors.size());
        return directors;
    }

    @Override
    public void removeDirector(long directorId) {
        final String DELETE_FROM_DIRECTORS_QUERY = "DELETE FROM directors WHERE id = ?";
        log.debug("Начало выполнения удаления режиссёра с id = {} из БД", directorId);

        int rowsDeleted = jdbcTemplate.update(DELETE_FROM_DIRECTORS_QUERY, directorId);

        log.info("Режиссёр с id '{}' {}. Операция завершена", directorId, rowsDeleted > 0 ? "удален" : "не был найден");
    }

    @Override
    public Director updateDirector(Director director) {
        final String UPDATE_DIRECTOR_QUERY = "UPDATE directors SET name = ? WHERE id = ?";
        log.debug("Начало выполнения обновления режиссёра с id = {} в БД", director.getId());

        jdbcTemplate.update(UPDATE_DIRECTOR_QUERY, director.getName(), director.getId());

        log.debug("Информация о режиссёре с id = {} была успешно обновлена", director.getId());
        return director; // Возвращаем обновленного режиссера
    }
}
