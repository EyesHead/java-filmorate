package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.service.validators.UserValidator;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCrudService {
    private final UserStorage userStorage;
    private final UserValidator userValidator;

    public Collection<User> getAll() {
        log.info("(NEW) Получен запрос на получение всех пользователей");
        Collection<User> users = userStorage.getAllUsers();
        log.info("(END) Возвращено {} пользователей", users.size());
        return users;
    }

    public User createUser(User user) {
        log.info("(NEW) Получен запрос на создание пользователя с логином = '{}'", user.getLogin());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User createdUser = userStorage.saveUser(user);
        log.info("(END) Пользователь с id '{}' создан", createdUser.getId());
        return createdUser;
    }

    public User updateUser(User user) {
        long userId = user.getId();
        log.info("(NEW) Получен запрос на обновление данных пользователя с id '{}'", userId);

        userValidator.checkUserOnExist(userId);
        User updatedUser = userStorage.updateUser(user);

        log.info("(END) Пользователь с id '{}' обновлен", updatedUser.getId());
        return updatedUser;
    }

    public User getUserById(long userId) {
        log.info("(NEW) Получен запрос на получение пользователя с ID = '{}'", userId);
        User user = userStorage.getUserById(userId).orElseThrow(
                () -> new NotFoundException("(END) Пользователь не найден. Id = " + userId)
        );
        log.info("(END) Пользователь с id '{}' найден", userId);
        return user;
    }

    public void deleteUserById(long userId) {
        log.info("(NEW) Получен запрос на удаление пользователя с id '{}'", userId);

        userValidator.checkUserOnExist(userId);
        userStorage.deleteUserById(userId);

        log.info("(END) Пользователь с id '{}' удален", userId);
    }
}
