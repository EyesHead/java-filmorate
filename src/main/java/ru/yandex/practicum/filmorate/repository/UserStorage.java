package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getAllUsers();

    User saveUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(long userId);

    void saveFriendToUser(long friendId, long userId);

    void removeFriend(long userId, long friendId);

    List<User> getUserFriends(long userId);

    List<User> getCommonFriends(long userId, long friendId);

    List<Long> getUserFriendsIds(long userId);

    void deleteUserById(long userId);
}
