package ru.yandex.practicum.filmorate.entity;

public enum EventOperation {
    REMOVE,
    ADD,
    UPDATE;

    public static EventOperation get(final int ordinal) {
        return EventOperation.values()[ordinal];
    }
}