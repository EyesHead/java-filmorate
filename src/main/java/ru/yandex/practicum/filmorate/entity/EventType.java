package ru.yandex.practicum.filmorate.entity;

public enum EventType {
    LIKE,
    REVIEW,
    FRIEND;

    public static EventType get(final int code) {
        return EventType.values()[code];
    }
}