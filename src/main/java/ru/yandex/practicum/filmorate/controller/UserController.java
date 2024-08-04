package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@RestController
@RequestMapping("/users")
@Slf4j(topic = "Users controller log")
@Validated
public class UserController {
    Map<Long, User> usersRepo = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        return usersRepo.values();
    }

    @PostMapping
    public User createNewUser(@Valid @RequestBody User user) {
        log.info("POST request by client received");
        var newUser = user.toBuilder()
                .name(getActualName(user))
                .id(generateUniqueId())
                .build();
        log.trace("PUT Generated user for create: {}", user);
        usersRepo.put(newUser.getId(), newUser);
        log.info("POST client response is created user with ID='{}'", newUser.getId());
        return newUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("PUT request by client received");
        validateOnUpdate(user);
        var updatedUser = user.toBuilder()
                .name(getActualName(user))
                .build();
        usersRepo.put(updatedUser.getId(), updatedUser);
        log.debug("(PUT) user was found in memory and updated: {}", updatedUser);
        log.info("(PUT) user was successfully updated");
        return updatedUser;
    }

    private String getActualName(User user) {
        if (user.getName() == null) {
            return user.getLogin();
        }
        return user.getName();
    }

    private void validateOnUpdate(User user) throws ValidationException {
        if (user.getId() == null) {
            log.debug("ID from request is null");
            throw new ValidationException("ID cannot be null for update");
        }
        usersRepo.keySet().stream()
                .filter(Objects::nonNull)
                .filter(oldUserId -> oldUserId.equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> {
                    log.debug("PUT: User not found in memory: ID={}", user.getId());
                    return new ValidationException("User was not found in memory");
                });
    }

    private long generateUniqueId() {
        long maxId = usersRepo.keySet().stream()
                .mapToLong(Long::longValue)
                .max().orElse(0L);
        return ++maxId;
    }
}
