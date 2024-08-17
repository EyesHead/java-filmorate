package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.dto.User;
import ru.yandex.practicum.filmorate.model.responses.success.ResponseMessage;
import ru.yandex.practicum.filmorate.validation.exceptions.NotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {
    Collection<User> getAll();

    User createUser(User user);

    User updateUser(User user) throws NotFoundException;

    Optional<User> getUserById(long userId) throws NotFoundException;

    ResponseMessage addFriendToUser(long userId, long friendId) throws NotFoundException;

    ResponseMessage removeFriend(Long userId, Long friendId);

    List<User> getAllFriendsFromUser(Long userId) throws NotFoundException;

    Set<User> getCommonFriends(Long userId, Long friendId) throws NotFoundException;
}
