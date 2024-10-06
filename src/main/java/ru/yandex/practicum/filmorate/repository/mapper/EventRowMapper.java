package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.entity.Event;
import ru.yandex.practicum.filmorate.entity.EventOperation;
import ru.yandex.practicum.filmorate.entity.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        EventOperation eventOperation = EventOperation.get(rs.getInt("operation_id"));
        EventType eventType = EventType.get(rs.getInt("type_id"));
        return Event.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .epochMilli(rs.getLong("event_timestamp"))
                .eventType(eventType)
                .operation(eventOperation)
                .entityId(rs.getLong("entity_id"))
                .build();
    }
}
