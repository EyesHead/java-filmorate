
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.entity.Mpa;
import ru.yandex.practicum.filmorate.repository.DbFilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FilmorateApplication.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbFilmStorageTest {
    private final DbFilmStorage dbFilmStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очистка таблиц перед каждым тестом для независимости тестов
        jdbcTemplate.update("DELETE FROM users_films_like");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
    }


    @Test
    void testGetFilmById() {
        // Arrange: Сначала нужно добавить фильм в базу данных
        Film film = Film.builder()
                .name("Inception")
                .description("A mind-bending thriller")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .mpa(new Mpa(1L, null))
                .build();

        Film savedFilm = dbFilmStorage.saveFilm(film);

        // Act
        Optional<Film> filmOptional = dbFilmStorage.getFilmById(savedFilm.getId());

        // Assert
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getId()).isEqualTo(savedFilm.getId());
                    assertThat(f.getName()).isEqualTo(savedFilm.getName());
                    assertThat(f.getDescription()).isEqualTo(savedFilm.getDescription());
                    assertThat(f.getReleaseDate()).isEqualTo(savedFilm.getReleaseDate());
                    assertThat(f.getDuration()).isEqualTo(savedFilm.getDuration());
                    assertThat(f.getMpa()).isEqualTo(savedFilm.getMpa());
                });
    }

    @Test
    void testGetAllFilms() {
        // Arrange: Добавим несколько фильмов
        Film film1 = Film.builder()
                .name("The Matrix")
                .description("A hacker discovers the truth about reality")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(new Mpa(4L, null))
                .build();

        Film film2 = Film.builder()
                .name("Interstellar")
                .description("A team travels through a wormhole in search of a new home for humanity")
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .mpa(new Mpa(3L, null))
                .build();

        dbFilmStorage.saveFilm(film1);
        dbFilmStorage.saveFilm(film2);

        // Act
        Collection<Film> films = dbFilmStorage.getAllFilms();

        // Assert
        assertThat(films)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("The Matrix", "Interstellar");
    }

    @Test
    void testGetMostLikedFilms() {
        // Arrange: Добавить несколько фильмов
        Film film1 = Film.builder()
                .name("The Dark Knight")
                .description("Batman battles the Joker")
                .releaseDate(LocalDate.of(2008, 7, 18))
                .duration(152)
                .mpa(new Mpa(4L, null))
                .build();

        Film film2 = Film.builder()
                .name("Pulp Fiction")
                .description("Interwoven stories of crime")
                .releaseDate(LocalDate.of(1994, 10, 14))
                .duration(154)
                .mpa(new Mpa(4L, null))
                .build();

        // Сохраняем фильмы в базу данных
        Film savedFilm1 = dbFilmStorage.saveFilm(film1);
        Film savedFilm2 = dbFilmStorage.saveFilm(film2);

        // Создаем пользователей
        long userId1 = 1L; // Идентификатор первого пользователя
        long userId2 = 2L; // Идентификатор второго пользователя
        long userId3 = 3L; // Идентификатор третьего пользователя

        // Добавляем пользователей в базу данных перед тем, как добавлять лайки
        jdbcTemplate.update("INSERT INTO users (user_id, username, email, login) VALUES (?, ?, ?, ?)", userId1, "User1", "user1@example.com", "user1login");
        jdbcTemplate.update("INSERT INTO users (user_id, username, email, login) VALUES (?, ?, ?, ?)", userId2, "User2", "user2@example.com", "user2login");
        jdbcTemplate.update("INSERT INTO users (user_id, username, email, login) VALUES (?, ?, ?, ?)", userId3, "User3", "user3@example.com", "user3login");


        // Добавляем лайки от пользователей к фильмам
        dbFilmStorage.saveLikeToFilm(savedFilm1.getId(), userId1);
        dbFilmStorage.saveLikeToFilm(savedFilm1.getId(), userId2);
        dbFilmStorage.saveLikeToFilm(savedFilm2.getId(), userId1);
        dbFilmStorage.saveLikeToFilm(savedFilm2.getId(), userId3);

        // Act: Получаем список самых популярных фильмов (например, топ 2)
        Collection<Film> mostLikedFilms = dbFilmStorage.getMostLikedFilms(2);

        // Assert: Проверяем, что фильмы возвращены в правильном порядке
        assertThat(mostLikedFilms)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactly("The Dark Knight", "Pulp Fiction");
    }

    @Test
    void testSaveFilmLikeToFilm() {
        // Arrange: Добавляем фильм и пользователя
        Film film = Film.builder()
                .name("Avatar")
                .description("A marine dispatched to the moon Pandora")
                .releaseDate(LocalDate.of(2009, 12, 18))
                .duration(162)
                .mpa(new Mpa(2L, null))
                .build();

        Film savedFilm = dbFilmStorage.saveFilm(film);
        jdbcTemplate.update("INSERT INTO users (user_id, username, email, login) VALUES (?, ?, ?, ?)", 1L, "User1", "user1@example.com", "user1login");

        // Act: Добавляем лайк от пользователя
        dbFilmStorage.saveLikeToFilm(savedFilm.getId(), 1L);

        // Assert: Проверяем, что лайк был успешно добавлен
        assertThat(dbFilmStorage.getUsersIdsWhoLikedFilm(savedFilm.getId()))
                .containsExactly(1L);
    }

    @Test
    void testDeleteLikeFromFilm() {
        // Arrange: Добавляем фильм и пользователя, ставим лайк
        Film film = Film.builder()
                .name("The Godfather")
                .description("The aging patriarch of an organized crime dynasty")
                .releaseDate(LocalDate.of(1972, 3, 24))
                .duration(175)
                .mpa(new Mpa(4L, null))
                .build();

        Film savedFilm = dbFilmStorage.saveFilm(film);
        jdbcTemplate.update("INSERT INTO users (user_id, username, email, login) VALUES (?, ?, ?, ?)", 1L, "User1", "user1@example.com", "user1login");

        dbFilmStorage.saveLikeToFilm(savedFilm.getId(), 1L);

        // Act: Удаляем лайк от пользователя
        boolean result = dbFilmStorage.deleteLikeFromFilm(savedFilm.getId(), 1L);

        // Assert: Проверяем, что лайк был удален
        assertThat(result).isTrue();
        assertThat(dbFilmStorage.getUsersIdsWhoLikedFilm(savedFilm.getId()))
                .doesNotContain(1L);
    }
}
