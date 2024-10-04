package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.jdbc.core.ResultSetExtractor;
import ru.yandex.practicum.filmorate.entity.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FilmDirectorsRowMapper implements ResultSetExtractor<Map<Long, Set<Director>>> {

    @Override
    public Map<Long, Set<Director>> extractData(ResultSet rs) throws SQLException {
        Map<Long, Set<Director>> filmDirectorsMap = new HashMap<>();

        while (rs.next()) {
            Long filmId = rs.getLong("film_id");
            Long directorId = rs.getLong("director_id");
            String directorName = rs.getString("name");

            Director director = new Director(directorId, directorName);

            filmDirectorsMap
                    .computeIfAbsent(filmId, k -> new HashSet<>())
                    .add(director);
        }

        return filmDirectorsMap;
    }
}
