package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.jdbc.core.ResultSetExtractor;
import ru.yandex.practicum.filmorate.entity.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FilmGenresRowMapper implements ResultSetExtractor<Map<Long, Set<Genre>>> {

    @Override
    public Map<Long, Set<Genre>> extractData(ResultSet rs) throws SQLException {
        Map<Long, Set<Genre>> filmGenresMap = new HashMap<>();

        while (rs.next()) {
            Long filmId = rs.getLong("film_id");
            Long genreId = rs.getLong("genre_id");
            String genreName = rs.getString("name");

            Genre genre = new Genre(genreId, genreName);

            // Получаем существующий набор жанров для фильма или создаём новый
            filmGenresMap
                    .computeIfAbsent(filmId, k -> new HashSet<>())
                    .add(genre);
        }

        return filmGenresMap;
    }
}
