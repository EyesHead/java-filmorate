package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.dto.User;
import ru.yandex.practicum.filmorate.model.responses.success.ResponseMessage;
import ru.yandex.practicum.filmorate.validation.exceptions.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User addUser(User user) {
        var newUser = user.toBuilder()
                .name(getActualName(user))
                .id(generateUniqueId())
                .friendsIds(new HashSet<>())
                .build();
        log.trace("Данные о новом пользователе: {}", newUser);
        users.put(newUser.getId(), newUser);
        log.info("Пользователь с именем '{}' был успешно создан и добавлен в репозиторий", newUser.getName());
        return newUser;
    }

    @Override
    public User updateUser(User newUser) throws NotFoundException {
        checkUserOnExist(newUser.getId());
        var updatedUser = newUser.toBuilder()
                .name(getActualName(newUser))
                .build();
        users.put(updatedUser.getId(), updatedUser);
        log.debug("Пользователь был успешно найден и обновлён: {}", updatedUser);
        log.info("Данные о пользователе с id =\"{}\" успешно обновлены", newUser.getId());
        return updatedUser;
    }

    @Override
    public Optional<User> getUserById(long userId) throws NotFoundException {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public ResponseMessage addFriendToUser(long userId, long friendId) throws NotFoundException {
        // Проверяем, существуют ли оба пользователя
        checkUserOnExist(userId);
        checkUserOnExist(friendId);

        // Получаем пользователей из хранилища
        User foundUser = users.get(userId);
        User foundFriend = users.get(friendId);

        // Проверяем, не является ли friendId уже другом пользователя
        if (foundUser.getFriendsIds().contains(friendId)) {
            throw new NotFoundException(String.format(
                    "Пользователь %d уже является другом для пользователя %d", friendId, userId));
        }

        foundUser.getFriendsIds().add(friendId);
        foundFriend.getFriendsIds().add(userId);
        log.info("Пользователь с id = {} и пользователь с id = {} теперь друг у друга в списке друзей",
                userId, friendId);

        return new ResponseMessage(String.format(
                "Пользователь %s c id = %d добавил в друзья пользователя %s c id = %d",
                foundUser.getName(), userId, foundFriend.getName(), friendId));
    }

    @Override
    public ResponseMessage removeFriend(Long userId, Long friendId) throws NotFoundException {
        // Проверяем, существуют ли оба пользователя
        checkUserOnExist(userId);
        checkUserOnExist(friendId);

        // Получаем пользователей из хранилища
        User foundUser = users.get(userId);
        User foundFriend = users.get(friendId);

        // Проверяем, являются ли два пользователя друзьями и удаляем, если являются
        if (!foundUser.getFriendsIds().remove(friendId) || !foundFriend.getFriendsIds().remove(userId)) {
            throw new NotFoundException(String.format(
                    "Пользователь %d не является другом для пользователя %d", friendId, userId));
        }

        log.info("Пользователь с id = {} удалил друга с id = {}", userId, friendId);

        return new ResponseMessage(String.format(
                "Пользователь %s c id = %d удалил из друзей пользователя %s c id = %d",
                foundUser.getName(), userId, foundFriend.getName(), friendId));
    }

    @Override
    public List<User> getAllFriendsFromUser(Long userId) {
        checkUserOnExist(userId);
        List<User> foundUserFriends = users.values()
                .stream()
                .filter(user -> user.getFriendsIds().contains(userId))
                .toList();
        if (foundUserFriends.isEmpty()) {
            throw new NotFoundException(String.format("У пользователя с id = %d нет друзей", userId));
        }
        return foundUserFriends;
    }

    @Override
    public Set<User> getCommonFriends(Long userId, Long friendId) throws NotFoundException {
        checkUserOnExist(userId);
        checkUserOnExist(friendId);

        User user = users.get(userId);
        User friend = users.get(friendId);

        // Нужно сделать так, чтобы

        Set<User> commonUsers = users.values()
                .stream()
                .filter(userStreamed -> user.getFriendsIds().contains(userStreamed.getId()) &&
                        friend.getFriendsIds().contains(userStreamed.getId()))
                .collect(Collectors.toSet());
        if (commonUsers.isEmpty()) {
            throw new NotFoundException(String.format(
                    "У пользователя %s с id = %d не обнаружено общих друзей с пользователем %s с id = %d",
                    user.getName(), userId, friend.getName(), friendId));
        }
        return commonUsers;
    }

    private void checkUserOnExist(long userId) throws NotFoundException {
        Optional.ofNullable(users.get(userId)).orElseThrow(
                () -> new NotFoundException(String.format("Пользователь с id=%d не найден", userId))
        );
    }

    private String getActualName(User user) {
        if (user.getName() == null) {
            log.info("Пользователь не указал имя. Вместо имени присвоен логин {}", user.getLogin());
            return user.getLogin();
        }
        return user.getName();
    }

    private long generateUniqueId() {
        long maxId = users.keySet().stream()
                .mapToLong(Long::longValue)
                .max().orElse(0L);
        return ++maxId;
    }
}
