package ru.yandex.practicum.filmorate.repository.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.entity.Genre;
import ru.yandex.practicum.filmorate.entity.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class FilmGenresRowMapper implements RowMapper<Film> {
    private final Map<Long, Set<Genre>> filmGenresMap;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        long filmId = rs.getLong("film_id");

        Mpa mpa = rs.getLong("mpa_id") == 0 ? null :
                new Mpa(rs.getLong("mpa_id"), rs.getString("mpa_name"));

        // Получаем жанры для текущего фильма из filmGenresMap
        Set<Genre> genres = filmGenresMap.getOrDefault(filmId, Set.of());


        return Film.builder()
                .id(filmId)
                .name(rs.getString("film_name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .genres(genres)
                .mpa(mpa)
                .build();
    }
}
