package ru.yandex.practicum.filmorate.service.validators;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.UserStorage;

@Slf4j
@AllArgsConstructor
@Component
public class UserValidator {
    private UserStorage userStorage;

    public void checkUserOnExist(long userId) throws NotFoundException {
        log.debug("(Validator) Начало проверки существования пользователя '{}' в БД.", userId);

        userStorage.getUserById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден. ID = " + userId)
        );

        log.debug("(Validator) Пользователь '{}' найден. Проверка выполнена.", userId);
    }
}