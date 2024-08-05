package ru.yandex.practicum.filmorate.serviceRepo;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class UserServiceRepo implements FilmorateRepository<User> {
    Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        var newUser = user.toBuilder()
                .name(getActualName(user))
                .id(generateUniqueId())
                .build();
        log.trace("POST Generated user for create: {}", user);
        users.put(newUser.getId(), newUser);
        log.info("POST client response is created user with ID='{}'", newUser.getId());
        return newUser;
    }

    @Override
    public User update(User user) {
        validateOnUpdate(user);
        var updatedUser = user.toBuilder()
                .name(getActualName(user))
                .build();
        users.put(updatedUser.getId(), updatedUser);
        log.debug("PUT user was found in memory and updated: {}", updatedUser);
        log.info("PUT user was successfully updated");
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
        users.keySet().stream()
                .filter(Objects::nonNull)
                .filter(oldUserId -> oldUserId.equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> {
                    log.debug("PUT: User not found in memory: ID={}", user.getId());
                    return new ValidationException("User was not found in memory");
                });
    }

    private long generateUniqueId() {
        long maxId = users.keySet().stream()
                .mapToLong(Long::longValue)
                .max().orElse(0L);
        return ++maxId;
    }
}
