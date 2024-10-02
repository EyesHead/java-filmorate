package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
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
public class DbLikeStorage implements LikeStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<Long, ArrayList<Long>> getMapOfLikesByPrimaryKey(List<Long> listOfIds, String primaryKey) {
        if (CollectionUtils.isEmpty(listOfIds)) return new HashMap<>();
        String secondaryKey = primaryKey.equals("user_id") ? "film_id" : "user_id";
        String inSql = String.join(",", Collections.nCopies(listOfIds.size(), "?"));
        final String GET_LIKED_FILMS_ID_BY_USER_ID = String.format("""
                SELECT film_id, user_id
                FROM users_films_like
                WHERE %s IN (%s)
                ORDER BY %s;
                """, primaryKey, inSql, primaryKey);
        return jdbcTemplate.query(GET_LIKED_FILMS_ID_BY_USER_ID, rs -> {
            Map<Long, ArrayList<Long>> result = new HashMap<>();
            while (rs.next()) {
                if (!result.containsKey(rs.getLong(primaryKey))) {
                    result.put(rs.getLong(primaryKey), new ArrayList<>(Arrays.asList(rs.getLong(secondaryKey))));
                } else {
                    result.get(rs.getLong(primaryKey)).add(rs.getLong(secondaryKey));
                }
            }
            return result;
        }, listOfIds.toArray());

    }
}
