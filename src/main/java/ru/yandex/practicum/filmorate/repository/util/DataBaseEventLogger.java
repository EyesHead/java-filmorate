package ru.yandex.practicum.filmorate.repository.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.entity.EventOperation;
import ru.yandex.practicum.filmorate.entity.EventType;
import ru.yandex.practicum.filmorate.repository.EventLogger;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Component
@Slf4j
public class DataBaseEventLogger implements EventLogger {
    private final JdbcTemplate jdbcTemplate;

    public void logEvent(long userId, EventType type, EventOperation operation, long entityId) {
        log.debug("Начало регистрации новой операция '{}' над '{}' от пользователя с id = '{}'. Id субъекта = '{}'",
                operation, type, userId, entityId);
        final int typeId = type.ordinal();
        final int operationId = operation.ordinal();
        final long eventTimestamp = System.currentTimeMillis();

        final String LOG_EVENT_QUERY = """
                INSERT INTO events (user_id, type_id, operation_id, entity_id, event_timestamp)
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(LOG_EVENT_QUERY, userId, typeId, operationId, entityId, eventTimestamp);
        log.debug("Операция успешно зарегестрирована");
    }
}