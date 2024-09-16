package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.User;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.service.util.UserValidator;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserFriendsService {
    private final UserStorage userStorage;
    private final UserValidator userValidator;

    public void addFriend(long userId, long friendId) {
        log.info("(NEW) Получен запрос от пользователя '{}' на добавление в друзья пользователя '{}'",
                userId, friendId);
        userValidator.checkUserOnExist(userId);
        userValidator.checkUserOnExist(friendId);

        userStorage.saveFriendToUser(userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        log.info("(NEW) Получен запрос от пользователя '{}' на удаление из друзей пользователя '{}'",
                userId, friendId);

        userValidator.checkUserOnExist(userId);
        userValidator.checkUserOnExist(friendId);

        userStorage.removeFriend(userId, friendId);
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
