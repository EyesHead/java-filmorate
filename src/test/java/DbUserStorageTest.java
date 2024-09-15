import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.entity.User;
import ru.yandex.practicum.filmorate.repository.DbUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FilmorateApplication.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbUserStorageTest {
    private final DbUserStorage dbUserStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очистка таблиц перед каждым тестом для независимости тестов
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM users_films_like");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void testSaveUser() {
        // Arrange
        User user = User.builder()
                .email("user1@example.com")
                .login("user1login")
                .name("User1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        // Act
        User savedUser = dbUserStorage.saveUser(user);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("user1@example.com");
    }

    @Test
    void testUpdateUser() {
        // Arrange
        User user = User.builder()
                .email("user2@example.com")
                .login("user2login")
                .name("User2")
                .birthday(LocalDate.of(1992, 2, 2))
                .build();
        User savedUser = dbUserStorage.saveUser(user);

        User updatedUser = savedUser.toBuilder()
                .name("UpdatedUser")
                .build();

        // Act
        User result = dbUserStorage.updateUser(updatedUser);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("UpdatedUser");
    }

    @Test
    void testGetUserById() {
        // Arrange
        User user = User.builder()
                .email("user3@example.com")
                .login("user3login")
                .name("User3")
                .birthday(LocalDate.of(1993, 3, 3))
                .build();
        User savedUser = dbUserStorage.saveUser(user);

        // Act
        Optional<User> userOptional = dbUserStorage.getUserById(savedUser.getId());

        // Assert
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(savedUser.getId());
                    assertThat(u.getEmail()).isEqualTo(savedUser.getEmail());
                });
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        User user1 = User.builder()
                .email("user1@example.com")
                .login("user1login")
                .name("User1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2login")
                .name("User2")
                .birthday(LocalDate.of(1992, 2, 2))
                .build();

        dbUserStorage.saveUser(user1);
        dbUserStorage.saveUser(user2);

        // Act
        Collection<User> users = dbUserStorage.getAllUsers();

        // Assert
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    void testSaveAndRemoveFriend() {
        // Arrange
        User user1 = User.builder()
                .email("user1@example.com")
                .login("user1login")
                .name("User1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2login")
                .name("User2")
                .birthday(LocalDate.of(1992, 2, 2))
                .build();

        User savedUser1 = dbUserStorage.saveUser(user1);
        User savedUser2 = dbUserStorage.saveUser(user2);

        // Act: Добавляем друга
        dbUserStorage.saveFriendToUser(savedUser1.getId(), savedUser2.getId());

        // Assert: Проверяем, что друг добавлен
        Collection<User> friends = dbUserStorage.getUserFriends(savedUser1.getId());
        assertThat(friends).hasSize(1).extracting(User::getEmail).containsExactly("user2@example.com");

        // Act: Удаляем друга
        dbUserStorage.removeFriend(savedUser1.getId(), savedUser2.getId());

        // Assert: Проверяем, что друг удалён
        friends = dbUserStorage.getUserFriends(savedUser1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        // Arrange
        User user1 = User.builder()
                .email("user1@example.com")
                .login("user1login")
                .name("User1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2login")
                .name("User2")
                .birthday(LocalDate.of(1992, 2, 2))
                .build();
        User commonFriend = User.builder()
                .email("common@example.com")
                .login("commonlogin")
                .name("CommonFriend")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        User savedUser1 = dbUserStorage.saveUser(user1);
        User savedUser2 = dbUserStorage.saveUser(user2);
        User savedCommonFriend = dbUserStorage.saveUser(commonFriend);

        dbUserStorage.saveFriendToUser(savedUser1.getId(), savedCommonFriend.getId());
        dbUserStorage.saveFriendToUser(savedUser2.getId(), savedCommonFriend.getId());

        // Act: Получаем общих друзей
        Collection<User> commonFriends = dbUserStorage.getCommonFriends(savedUser1.getId(), savedUser2.getId());

        // Assert: Проверяем, что общий друг возвращён
        assertThat(commonFriends).hasSize(1).extracting(User::getEmail).containsExactly("common@example.com");
    }
}
