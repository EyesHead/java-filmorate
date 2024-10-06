package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbLikeStorage implements LikeStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<Long, ArrayList<Long>> getMapOfLikesByPrimaryKey(List<Long> listOfIds, String primaryKey) {
        if (CollectionUtils.isEmpty(listOfIds)) {
            log.debug("(Repo) Получен пустой список идентификаторов. Возвращается пустая карта лайков.");
            return new HashMap<>();
        }

        String secondaryKey = primaryKey.equals("user_id") ? "film_id" : "user_id";
        String inSql = String.join(",", Collections.nCopies(listOfIds.size(), "?"));
        final String GET_LIKED_FILMS_ID_BY_USER_ID = String.format("""
                SELECT film_id, user_id
                FROM users_films_like
                WHERE %s IN (%s)
                ORDER BY %s;
                """, primaryKey, inSql, primaryKey);

        log.debug("(Repo) Выполнение запроса для получения лайков. Первичный ключ: '{}', вторичный ключ: '{}'.",
                primaryKey, secondaryKey);

        Map<Long, ArrayList<Long>> result = jdbcTemplate.query(GET_LIKED_FILMS_ID_BY_USER_ID, rs -> {
            Map<Long, ArrayList<Long>> tempResult = new HashMap<>();
            while (rs.next()) {
                long key = rs.getLong(primaryKey);
                long value = rs.getLong(secondaryKey);

                if (!tempResult.containsKey(key)) {
                    tempResult.put(key, new ArrayList<>(List.of(value)));
                } else {
                    tempResult.get(key).add(value);
                }
            }
            return tempResult;
        }, listOfIds.toArray());

        log.debug("(Repo) Запрос выполнен. Размер возвращаемой карты: '{}'. Ключи ({}): {}, Значения ({}): {}",
                result.size(),
                primaryKey, result.keySet(),
                secondaryKey, result.values());

        return result;
    }
}

