package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.model.dto.User;
import ru.yandex.practicum.filmorate.model.dto.Marker;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class CrudUserService {
    private final UserStorage userStorage;

    public Collection<User> getAll() {
        log.info("Получен запрос на получение всех пользователей");
        return userStorage.getAll();
    }

    public User create(@Validated(Marker.OnCreate.class) User user) {
        log.info("Получен запрос на создание от пользователя с логином = '{}'", user.getLogin());
        return userStorage.addUser(user);
    }

    public User update(@Validated(Marker.OnUpdate.class) User user) {
        log.info("Получен запрос на обновления данных о пользователе");
        return userStorage.updateUser(user);
    }
}
