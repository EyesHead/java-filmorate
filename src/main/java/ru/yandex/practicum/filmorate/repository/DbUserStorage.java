package ru.yandex.practicum.filmorate.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.entity.User;
import ru.yandex.practicum.filmorate.repository.mapper.UserRowMapper;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DbUserStorage implements UserStorage {
    JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> getAllUsers() {
        log.debug("(Repo) Начало выполнения запроса на получение всех пользователей.");

        List<User> users = jdbcTemplate.query("""
                SELECT user_id, email, login, username, birthday
                FROM users
                """, new UserRowMapper());

        if (users.isEmpty()) {
            log.debug("(Repo) Запрос завершен. Пользователи не найдены.");
        } else {
            log.debug("(Repo) Запрос завершен. Количество найденных пользователей: {}. Пользователи: {}", users.size(), users);
        }

        return users;
    }

    @Override
    public User saveUser(User user) {
        log.debug("(Repo) Начало сохранения нового пользователя в базу данных. Пользователь: {}", user);

        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("username", user.getName());
        parameters.put("birthday", user.getBirthday());

        log.trace("(Repo) Подготовленные параметры для вставки пользователя в базу: {}", parameters);

        long userId = jdbcInsert.executeAndReturnKey(parameters).longValue();
        log.trace("(Repo) Пользователь успешно добавлен. Сгенерированный id: {}", userId);

        User savedUser = user.toBuilder().id(userId).build();
        log.debug("(Repo) Пользователь успешно создан и записан в базу данных. Пользователь: {}", savedUser);

        return savedUser;
    }

    @Override
    public User updateUser(User user) {
        log.debug("(Repo) Начало обновления данных пользователя с id = {}. Пользователь: {}", user.getId(), user);

        jdbcTemplate.update("""
                UPDATE users
                SET email = ?, login = ?, username = ?, birthday = ?
                WHERE user_id = ?
                """, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        log.debug("(Repo) Пользователь с id = {} успешно обновлен. Обновленные данные: {}", user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getUserById(long userId) {
        log.debug("(Repo) Выполнение запроса на получение пользователя с id = {}", userId);

        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE user_id = ?",
                    new UserRowMapper(),
                    userId);

            List<Long> friendsIds = getUserFriendsIds(userId);
            User userWithFriends = user.toBuilder()
                    .friendsIds(friendsIds)
                    .build();
            log.debug("(Repo) Пользователь с id = {} найден: {}", userId, userWithFriends);

            return Optional.of(userWithFriends);
        } catch (EmptyResultDataAccessException e) {
            log.warn("(Repo) Пользователь с id = {} не найден в базе данных.", userId);
            return Optional.empty();
        }
    }

    @Override
    public List<Long> getUserFriendsIds(long userId) {
        log.debug("(Repo) Начало выполнения запроса на получение id друзей пользователя с id = {}.", userId);

        List<Long> friendIds = jdbcTemplate.query(
                "SELECT friend_id FROM friendship WHERE user_id = ?",
                (rs, rowNum) -> rs.getLong("friend_id"),
                userId);

        if (friendIds.isEmpty()) {
            log.debug("(Repo) Пользователь с id = {} не имеет друзей.", userId);
        } else {
            log.debug("(Repo) Id друзей пользователя с id = {} успешно получены. Друзья: {}", userId, friendIds);
        }

        return friendIds;
    }

    @Override
    public void saveFriendToUser(long friendId, long userId) {
        log.debug("(Repo) Начало добавления друга с id = {} пользователю с id = {}.", friendId, userId);
        jdbcTemplate.update("""
                INSERT INTO friendship(user_id, friend_id) VALUES (?, ?)
                """, userId, friendId);
        log.debug("(Repo) Друг с id = {} успешно добавлен пользователю с id = {}.", friendId, userId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        log.debug("(Repo) Начало удаления друга с id = {} у пользователя с id = {}.", friendId, userId);

        int rowsAffected = jdbcTemplate.update("""
                DELETE FROM friendship
                WHERE user_id = ? AND friend_id = ?
                """, userId, friendId);
        if (rowsAffected == 0) {
            log.info("(Repo) У пользователя ID='{}' не было друга ID='{}'", userId, friendId);
        } else {
            log.info("(Repo) Друг с id = {} успешно удален у пользователя с id = {}.", friendId, userId);
        }
    }

    @Override
    public List<User> getUserFriends(long userId) {
        log.debug("(Repo) Начало выполнения запроса на получение списка друзей пользователя с id = {}.", userId);

        List<User> friends = jdbcTemplate.query("""
                SELECT u.*
                FROM friendship f
                JOIN users u ON f.friend_id = u.user_id
                WHERE f.user_id = ?
                """, new UserRowMapper(), userId);

        log.debug("(Repo) Запрос завершен. Найдено друзей: {}. Друзья: {}", friends.size(), friends);

        return friends;
    }

    @Override
    public List<User> getCommonFriends(long userId, long friendId) {
        log.debug("(Repo) Начало выполнения запроса на получение общих друзей пользователей с id = {} и id = {}.", userId, friendId);

        List<User> commonFriends = jdbcTemplate.query("""
                SELECT u.*
                FROM friendship f1
                JOIN friendship f2 ON f1.friend_id = f2.friend_id
                JOIN users u ON f1.friend_id = u.user_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                """, new UserRowMapper(), userId, friendId);

        log.debug("(Repo) Запрос завершен. Найдено общих друзей: {}.", commonFriends.size());

        return commonFriends;
    }

    @Override
    public void deleteUserById(long userId) {
        log.debug("(Repo) Начало удаления пользователя с id = {}.", userId);
        jdbcTemplate.update("""
                DELETE FROM users WHERE user_id = ?
                """, userId);
        log.debug("(Repo) Пользователь с id = {} успешно удален.", userId);
    }
}

