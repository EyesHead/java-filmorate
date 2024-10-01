package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static ru.yandex.practicum.filmorate.entity.EventOperation.*;
import static ru.yandex.practicum.filmorate.entity.EventType.*;
import ru.yandex.practicum.filmorate.entity.User;
import ru.yandex.practicum.filmorate.repository.EventLogger;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.service.validators.UserValidator;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserFriendsService {
    private final UserStorage userStorage;
    private final EventLogger eventLogger;

    private final UserValidator userValidator;

    public void addFriend(long userId, long friendId) {
        log.info("(NEW) Получен запрос от пользователя '{}' на добавление в друзья пользователя '{}'",
                userId, friendId);
        userValidator.checkUserOnExist(userId);
        userValidator.checkUserOnExist(friendId);

        userStorage.saveFriendToUser(userId, friendId);
        eventLogger.logEvent(userId, FRIEND, ADD, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        log.info("(NEW) Получен запрос от пользователя '{}' на удаление из друзей пользователя '{}'",
                userId, friendId);

        userValidator.checkUserOnExist(userId);
        userValidator.checkUserOnExist(friendId);

        userStorage.removeFriend(userId, friendId);
        eventLogger.logEvent(userId, FRIEND, REMOVE, friendId);
    }

    public List<User> getUserFriends(long userId) {
        log.info("(NEW) Получен запрос от пользователя '{}' на получение списка всех друзей",
                userId);

        userValidator.checkUserOnExist(userId);

        return userStorage.getUserFriends(userId);
    }

    public List<User> getCommonFriends(long userId, long friendId) {
        log.info("(NEW) Получен запрос от пользователя '{}' на поиск общих друзей с пользователем '{}'",
                userId, friendId);

        userValidator.checkUserOnExist(userId);
        userValidator.checkUserOnExist(friendId);

        return userStorage.getCommonFriends(userId, friendId);
    }
}