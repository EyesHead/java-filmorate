package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.entity.EventOperation;
import ru.yandex.practicum.filmorate.entity.EventType;

public interface EventLogger {
    void logEvent(long userId, EventType type, EventOperation operation, long entityId);
}
