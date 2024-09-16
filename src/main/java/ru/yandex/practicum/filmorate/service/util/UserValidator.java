package ru.yandex.practicum.filmorate.service.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.repository.UserStorage;

@Slf4j
@AllArgsConstructor
@Component
public class UserValidator {
    private UserStorage userStorage;

    public void checkUserOnExist(long userId) throws ValidationException {
        log.debug("Проверка существования пользователя '{}' в БД.", userId);

        userStorage.getUserById(userId).orElseThrow(
                () -> new ValidationException("Пользователь не найден. ID = " + userId));

        log.debug("Пользователь '{}' найден. Проверка выполнена", userId);
    }
}