package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.entity.Event;

import java.util.List;

public interface EventStorage {
    List<Event> getUserEvents(long eventId);
}
