package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.entity.Event;
import ru.yandex.practicum.filmorate.repository.mapper.EventRowMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbEventStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Event> getUserEvents(long userId) {
        final String GET_EVENTS_BY_USER_ID_QUERY = """
                SELECT *
                FROM events
                WHERE user_id = ?
                """;
        log.debug("Выполнение запроса для получения всех событий, связанных с пользователем '{}'", userId);

        List<Event> foundEvents = jdbcTemplate.query(
                GET_EVENTS_BY_USER_ID_QUERY,
                    new EventRowMapper(),
                    userId);
        if (foundEvents.isEmpty()) {
            log.info("Ещё нет событий, связанных с пользователем '{}'", userId);
        } else {
            log.info("У пользователя '{}' найдено '{}' событий", userId, foundEvents.size());
        }

        return foundEvents;
    }
}
