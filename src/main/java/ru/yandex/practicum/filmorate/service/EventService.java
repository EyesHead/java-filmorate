package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Event;
import ru.yandex.practicum.filmorate.repository.EventStorage;
import ru.yandex.practicum.filmorate.service.validators.UserValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventStorage eventStorage;
    private final UserValidator userValidator;

    public List<Event> getUserEvents(long userId) {
        log.info("(NEW) Получен запрос на получение ленты событий пользователя '{}'",
                userId);

        userValidator.checkUserOnExist(userId);

        List<Event> foundEvents = eventStorage.getUserEvents(userId);
        log.info("(END) У пользователя c id = {} было найдено '{}' событий", userId, foundEvents.size());

        return foundEvents;
    }
}
