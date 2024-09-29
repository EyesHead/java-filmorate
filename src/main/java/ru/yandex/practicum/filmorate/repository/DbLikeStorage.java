package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DbLikeStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Long> getLikedFilmsIdByUserId(Long userId) {
        final String GET_LIKED_FILMS_ID_BY_USER_ID = """
                SELECT film_id
                FROM users_films_like
                WHERE user_id = ?
                """;
        return jdbcTemplate.queryForList(GET_LIKED_FILMS_ID_BY_USER_ID, Long.class, userId);
    }

    public Map<Long, ArrayList<Long>> getUsersIdThatLikedFilms(List<Long> filmIds) {
        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        final String GET_LIKED_FILMS_ID_BY_USER_ID = String.format("""
                SELECT film_id, user_id
                FROM users_films_like
                WHERE film_id IN (%s)
                ORDER BY film_id
                """, inSql);
        return jdbcTemplate.query(GET_LIKED_FILMS_ID_BY_USER_ID, rs -> {
            Map<Long, ArrayList<Long>> result = new HashMap<>();
            while(rs.next()) {
                if(!result.containsKey(rs.getLong("film_id"))) {
                    result.put(rs.getLong("film_id"), new ArrayList<>(Arrays.asList(rs.getLong("user_id"))));
                } else {
                    result.get(rs.getLong("film_id")).add(rs.getLong("user_id"));
                }
            }
            return result;
        }, filmIds.toArray());
    }
}
