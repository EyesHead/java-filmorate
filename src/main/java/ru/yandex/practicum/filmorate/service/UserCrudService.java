package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.entity.User;
import ru.yandex.practicum.filmorate.entity.Marker;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.service.util.UserValidator;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserCrudService {
    private final UserStorage userStorage;
    private final UserValidator userValidator;

    public Collection<User> getAll() {
        log.info("(NEW) Получен запрос на получение всех пользователей");
        return userStorage.getAllUsers();
    }

    public User createUser(@Validated(Marker.OnCreate.class) User user) {
        log.info("(NEW) Получен запрос на создание пользователя '{}' с логином = '{}'", user.getName(), user.getLogin());

        if (user.getName() == null) {
            user = user.toBuilder().name(user.getLogin()).build();
        }

        return userStorage.saveUser(user);
    }

    public User updateUser(@Validated(Marker.OnUpdate.class) User user) {
        long userId = user.getId();
        log.info("(NEW) Получен запрос на обновления данных о пользователе '{}'.", userId);
        userValidator.checkUserOnExist(userId);
        return userStorage.updateUser(user);
    }

    public User getUserById(long userId) {
        log.info("(NEW) Получен новый запрос на получение пользователя с ID = {}.", userId);
        return userStorage.getUserById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден. Id = " + userId)
        );
    }

    public void deleteUserById(long userId) {
        log.info("(NEW) Получен запрос на удаление пользователя с id '{}'.", userId);
        userValidator.checkUserOnExist(userId);
        userStorage.deleteUserById(userId);
    }
}
