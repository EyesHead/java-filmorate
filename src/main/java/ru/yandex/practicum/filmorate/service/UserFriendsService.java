package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.User;
import ru.yandex.practicum.filmorate.repository.EventLogger;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.service.validators.UserValidator;

import java.util.List;

import static ru.yandex.practicum.filmorate.entity.EventOperation.ADD;
import static ru.yandex.practicum.filmorate.entity.EventOperation.REMOVE;
import static ru.yandex.practicum.filmorate.entity.EventType.FRIEND;

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

        userStorage.saveFriendToUser(friendId, userId);
        eventLogger.logEvent(userId, FRIEND, ADD, friendId);
        log.info("(END) Пользователь '{}' успешно добавил пользователя '{}' в друзья.", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        log.info("(NEW) Получен запрос от пользователя '{}' на удаление из друзей пользователя '{}'",
                userId, friendId);

        userValidator.checkUserOnExist(userId);
        userValidator.checkUserOnExist(friendId);

        userStorage.removeFriend(userId, friendId);
        eventLogger.logEvent(userId, FRIEND, REMOVE, friendId);
        log.info("(END) Пользователь '{}' успешно удалил пользователя '{}' из друзей.", userId, friendId);
    }

    public List<User> getUserFriends(long userId) {
        log.info("(NEW) Получен запрос от пользователя '{}' на получение списка всех друзей", userId);

        userValidator.checkUserOnExist(userId);

        List<User> userFriends = userStorage.getUserFriends(userId);

        if (userFriends.isEmpty()) {
            log.warn("(END) У пользователя с id = {} нет друзей.", userId);
        } else {
            log.info("(END) У пользователя с id = {} найдено {} друзей: {}", userId, userFriends.size(), userFriends);
        }

        return userFriends;
    }

    public List<User> getCommonFriends(long userId, long friendId) {
        log.info("(NEW) Получен запрос от пользователя '{}' на поиск общих друзей с пользователем '{}'",
                userId, friendId);

        userValidator.checkUserOnExist(userId);
        userValidator.checkUserOnExist(friendId);

        List<User> commonFriends = userStorage.getCommonFriends(userId, friendId);

        if (commonFriends.isEmpty()) {
            log.warn("(END) У пользователей с id = {} и id = {} нет общих друзей", userId, friendId);
        } else {
            log.info("(END) У пользователей с id = {} и id = {} найдено {} общих друзей: {}",
                    userId, friendId, commonFriends.size(), commonFriends);
        }

        return commonFriends;
    }
}