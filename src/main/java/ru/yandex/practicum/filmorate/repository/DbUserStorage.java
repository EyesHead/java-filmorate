package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
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
public class DbUserStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> getAllUsers() {
        final String GET_ALL_USERS_QUERY = """
                SELECT user_id, email, login, username, birthday
                FROM users
                """;
        log.debug("Выполнение запроса на получение всех пользователей. Запрос: {}", GET_ALL_USERS_QUERY);

        List<User> users = jdbcTemplate.query(GET_ALL_USERS_QUERY, new UserRowMapper());
        log.debug("Получены все пользователи: {}", users);

        return users;
    }

    @Override
    public User saveUser(User user) {
        log.debug("Сохранение нового пользователя в базу данных. Пользователь: {}", user);

        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("username", user.getName());
        parameters.put("birthday", user.getBirthday());

        long userId = jdbcInsert.executeAndReturnKey(parameters).longValue();
        log.debug("Пользователь успешно добавлен. Сгенерированный id: {}", userId);

        User savedUser = user.toBuilder().id(userId).build();
        log.info("Пользователь успешно создан и записан в базу данных. Пользователь: {}", savedUser);

        return savedUser;
    }

    @Override
    public User updateUser(User user) {
        log.debug("Запрос на обновление данных о пользователе в БД. Пользователь: {}", user);

        final String UPDATE_USER_QUERY = """
                UPDATE users
                SET email = ?, login = ?, username = ?, birthday = ?
                WHERE user_id = ?
                """;

        jdbcTemplate.update(UPDATE_USER_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        log.debug("Обновление данных о пользователе с id = {} успешно выполнено", user.getId());

        return user;
    }

    @Override
    public Optional<User> getUserById(long userId) {
        final String GET_USER_BY_ID_WITHOUT_FRIENDS_IDS_QUERY =
                "SELECT * FROM users WHERE user_id = ?";

        try {
            log.debug("Выполнение запроса на получение пользователя с id = {}. Запрос: {}", userId, GET_USER_BY_ID_WITHOUT_FRIENDS_IDS_QUERY);
            User user = jdbcTemplate.queryForObject(
                    GET_USER_BY_ID_WITHOUT_FRIENDS_IDS_QUERY,
                    new UserRowMapper(),
                    userId);
            log.debug("Пользователь найден: {}", user);

            List<Long> friendsIds = getUserFriendsIds(userId);
            User userWithFriends = user.toBuilder()
                    .friendsIds(friendsIds)
                    .build();
            log.debug("Пользователь с id = {} с друзьями: {}", userId, userWithFriends);

            return Optional.of(userWithFriends);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь с id = {} не найден в базе данных.", userId);
            return Optional.empty();
        }
    }

    @Override
    public List<Long> getUserFriendsIds(long userId) {
        final String GET_USER_FRIENDS_IDS_QUERY = "SELECT friend_id FROM friendship WHERE user_id = ?";
        log.debug("Выполнение запроса на получение id друзей пользователя с id = {}. Запрос: {}", userId, GET_USER_FRIENDS_IDS_QUERY);

        List<Long> friendIds = jdbcTemplate.query(
                GET_USER_FRIENDS_IDS_QUERY,
                (rs, rowNum) -> rs.getLong("friend_id"),
                userId);

        log.debug("Id друзей пользователя с id = {}: {}", userId, friendIds);
        return friendIds;
    }

    @Override
    public void saveFriendToUser(long userId, long friendId) {
        final String INSERT_FRIEND_TO_USER_QUERY = "INSERT INTO friendship(user_id, friend_id) VALUES (?, ?)";
        log.debug("Добавление друга с id = {} пользователю с id = {}. Запрос: {}", friendId, userId, INSERT_FRIEND_TO_USER_QUERY);

        jdbcTemplate.update(INSERT_FRIEND_TO_USER_QUERY, userId, friendId);
        log.info("Друг с id = {} успешно добавлен пользователю с id = {}", friendId, userId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        final String DELETE_USER_FRIENDS_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        log.debug("Удаление друга с id = {} у пользователя с id = {}. Запрос: {}", friendId, userId, DELETE_USER_FRIENDS_QUERY);

        jdbcTemplate.update(DELETE_USER_FRIENDS_QUERY, userId, friendId);
        log.info("Друг с id = {} успешно удален у пользователя с id = {}", friendId, userId);
    }

    @Override
    public List<User> getUserFriends(long userId) {
        final String GET_FRIENDS_QUERY = """
                SELECT u.user_id, u.email, u.login, u.username, u.birthday
                FROM friendship f
                JOIN users u ON f.friend_id = u.user_id
                WHERE f.user_id = ?
                """;

        log.debug("Получение списка друзей пользователя с id = {}. Запрос: {}", userId, GET_FRIENDS_QUERY);
        List<User> friends = jdbcTemplate.query(GET_FRIENDS_QUERY, new UserRowMapper(), userId);
        log.debug("Друзья пользователя с id = {}: {}", userId, friends);

        return friends;
    }

    @Override
    public List<User> getCommonFriends(long userId, long friendId) {
        String sql = "SELECT u.* " +
                "FROM friendship f1 " +
                "JOIN friendship f2 ON f1.friend_id = f2.friend_id " +
                "JOIN users u ON f1.friend_id = u.user_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";

        log.debug("Получение общих друзей для пользователей с id = {} и id = {}. Запрос: {}", userId, friendId, sql);
        List<User> commonFriends = jdbcTemplate.query(sql, new UserRowMapper(), userId, friendId);
        log.debug("Общие друзья пользователей с id = {} и id = {}: {}", userId, friendId, commonFriends);

        return commonFriends;
    }
}
