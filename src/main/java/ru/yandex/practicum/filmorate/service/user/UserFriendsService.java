package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.dto.User;
import ru.yandex.practicum.filmorate.model.responses.success.ResponseMessage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserFriendsService {
    private final UserStorage userStorage;

    public ResponseMessage addFriend(long userId, long friendId) {
        log.info("Получен запрос от пользователя '{}' на добавление в друзья пользователя '{}'",
                userId, friendId);

        return userStorage.addFriendToUser(userId, friendId);
    }

    public ResponseMessage removeFriend(Long userId, Long friendId) {
        log.info("Получен запрос от пользователя '{}' на удаление из друзей пользователя '{}'",
                userId, friendId);
        return userStorage.removeFriend(userId, friendId);
    }

    public List<User> getAllFriends(Long userId) {
        log.info("Получен запрос от пользователя '{}' на получение списка всех друзей",
                userId);
        return userStorage.getAllFriendsFromUser(userId);
    }

    public Set<User> getCommonFriends(Long userId, Long anotherUserId) {
        log.info("Получен запрос от пользователя '{}' на поиск общих друзей с пользователем '{}'",
                userId, anotherUserId);
        return userStorage.getCommonFriends(userId, anotherUserId);
    }
}
