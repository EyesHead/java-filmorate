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
        final String GET_ALL_USERS_QUERY = """
                SELECT user_id, email, login, username, birthday
                FROM users
                """;
        log.debug("Начало выполнения запроса на получение всех пользователей. Запрос: {}", GET_ALL_USERS_QUERY);

        List<User> users = jdbcTemplate.query(GET_ALL_USERS_QUERY, new UserRowMapper());

        if (users.isEmpty()) {
            log.info("Запрос завершен. Пользователи не найдены.");
        } else {
            log.info("Запрос завершен. Количество найденных пользователей: {}. Пользователи: {}", users.size(), users);
        }

        return users;
    }


    @Override
    public User saveUser(User user) {
        log.debug("Начало сохранения нового пользователя в базу данных. Пользователь: {}", user);

        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("username", user.getName());
        parameters.put("birthday", user.getBirthday());

        log.trace("Подготовленные параметры для вставки пользователя в базу: {}", parameters);

        long userId = jdbcInsert.executeAndReturnKey(parameters).longValue();
        log.debug("Пользователь успешно добавлен. Сгенерированный id: {}", userId);

        User savedUser = user.toBuilder().id(userId).build();
        log.info("Пользователь успешно создан и записан в базу данных. Пользователь: {}", savedUser);

        return savedUser;
    }


    @Override
    public User updateUser(User user) {
        log.debug("Начало обновления данных пользователя с id = {}. Пользователь: {}", user.getId(), user);

        final String UPDATE_USER_QUERY = """
                UPDATE users
                SET email = ?, login = ?, username = ?, birthday = ?
                WHERE user_id = ?
                """;

        log.trace("Подготовка запроса для обновления данных пользователя. Запрос: {}", UPDATE_USER_QUERY);
        log.trace("Параметры запроса: email = {}, login = {}, username = {}, birthday = {}, user_id = {}",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        jdbcTemplate.update(UPDATE_USER_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        log.info("Пользователь с id = {} успешно обновлен. Обновленные данные: {}", user.getId(), user);
        return user;
    }


    @Override
    public Optional<User> getUserById(long userId) {
        final String GET_USER_BY_ID_WITHOUT_FRIENDS_IDS_QUERY =
                "SELECT * FROM users WHERE user_id = ?";

        try {
            log.debug("Выполнение запроса на получение пользователя с id = {}. Запрос: {}",
                    userId, GET_USER_BY_ID_WITHOUT_FRIENDS_IDS_QUERY);
            User user = jdbcTemplate.queryForObject(
                    GET_USER_BY_ID_WITHOUT_FRIENDS_IDS_QUERY,
                    new UserRowMapper(),
                    userId);

            List<Long> friendsIds = getUserFriendsIds(userId);
            User userWithFriends = user.toBuilder()
                    .friendsIds(friendsIds)
                    .build();
            log.debug("Пользователь с id = {} найден: {}", userId, userWithFriends);

            return Optional.of(userWithFriends);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь с id = {} не найден в базе данных.", userId);
            return Optional.empty();
        }
    }

    @Override
    public List<Long> getUserFriendsIds(long userId) {
        final String GET_USER_FRIENDS_IDS_QUERY = "SELECT friend_id FROM friendship WHERE user_id = ?";
        log.debug("Начало выполнения запроса на получение id друзей пользователя с id = {}. Запрос: {}", userId, GET_USER_FRIENDS_IDS_QUERY);

        List<Long> friendIds = jdbcTemplate.query(
                GET_USER_FRIENDS_IDS_QUERY,
                (rs, rowNum) -> rs.getLong("friend_id"),
                userId);

        if (friendIds.isEmpty()) {
            log.info("Пользователь с id = {} не имеет друзей.", userId);
        } else {
            log.info("Id друзей пользователя с id = {} успешно получены. Друзья: {}", userId, friendIds);
        }

        return friendIds;
    }


    @Override
    public void saveFriendToUser(long userId, long friendId) {
        final String INSERT_FRIEND_TO_USER_QUERY = "INSERT INTO friendship(user_id, friend_id) VALUES (?, ?)";
        log.debug("Начало добавления друга с id = {} пользователю с id = {}. Запрос: {}", friendId, userId, INSERT_FRIEND_TO_USER_QUERY);

        int rowsAffected = jdbcTemplate.update(INSERT_FRIEND_TO_USER_QUERY, userId, friendId);

        log.debug("Запрос на добавление друга выполнен. Затронуто строк: {}", rowsAffected);
        log.info("Друг с id = {} успешно добавлен пользователю с id = {}", friendId, userId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        final String DELETE_USER_FRIENDS_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        log.debug("Начало удаления друга с id = {} у пользователя с id = {}. Запрос: {}", friendId, userId, DELETE_USER_FRIENDS_QUERY);

        int rowsAffected = jdbcTemplate.update(DELETE_USER_FRIENDS_QUERY, userId, friendId);
        if (rowsAffected == 0) {
            log.info("У пользователя ID='{}' не было друга ID='{}'", userId, friendId);
        } else {
            log.info("Друг с id = {} успешно удален у пользователя с id = {}", friendId, userId);
        }
    }

    @Override
    public List<User> getUserFriends(long userId) {
        final String GET_FRIENDS_QUERY = """
                SELECT u.user_id, u.email, u.login, u.username, u.birthday
                FROM friendship f
                JOIN users u ON f.friend_id = u.user_id
                WHERE f.user_id = ?
                """;

        log.debug("Начало выполнения запроса на получение списка друзей пользователя с id = {}. Запрос: {}", userId, GET_FRIENDS_QUERY);

        List<User> friends = jdbcTemplate.query(GET_FRIENDS_QUERY, new UserRowMapper(), userId);

        if (friends.isEmpty()) {
            log.info("Пользователь с id = {} не имеет друзей.", userId);
        } else {
            log.info("Список друзей пользователя с id = {} успешно получен. Друзья: {}", userId, friends);
        }

        return friends;
    }

    @Override
    public List<User> getCommonFriends(long userId, long friendId) {
        String sql = """
                SELECT u.*
                FROM friendship f1
                JOIN friendship f2 ON f1.friend_id = f2.friend_id
                JOIN users u ON f1.friend_id = u.user_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                """;

        log.debug("Начало выполнения запроса на получение общих друзей пользователей с id = {} и id = {}. Запрос: {}", userId, friendId, sql);

        List<User> commonFriends = jdbcTemplate.query(sql, new UserRowMapper(), userId, friendId);

        if (commonFriends.isEmpty()) {
            log.info("Общие друзья для пользователей с id = {} и id = {} не найдены.", userId, friendId);
        } else {
            log.info("Общие друзья для пользователей с id = {} и id = {} успешно получены. Общие друзья: {}", userId, friendId, commonFriends);
        }

        return commonFriends;
    }

    @Override
    public void deleteUserById(long userId) {
        final String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
}
