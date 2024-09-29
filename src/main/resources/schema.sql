DROP TABLE IF EXISTS mpa, users, films, genres, films_genres, users_films_like, friendship, reviews, reviews_likes;

-- Таблица mpa
CREATE TABLE IF NOT EXISTS mpa (
    mpa_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mpa_name VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    login VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(150),
    birthday DATE
);

-- Таблица films
CREATE TABLE IF NOT EXISTS films (
    film_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    film_name VARCHAR(150) NOT NULL,
    description VARCHAR(200),
    release_date DATE,
    duration INTEGER CHECK(duration > 0),
    mpa_id BIGINT REFERENCES mpa(mpa_id)
);

-- Таблица всех жанров. Названия жанров уникальны
CREATE TABLE IF NOT EXISTS genres (
    genre_id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Удаляется film - удаляется запись в films_genres
-- Удаление genre запрещено
CREATE TABLE IF NOT EXISTS films_genres (
    film_id BIGINT REFERENCES films(film_id) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES genres(genre_id) ON DELETE RESTRICT,
    PRIMARY KEY (film_id, genre_id)
);

-- Таблица связи many-to-many между пользователями и фильмами (для лайков)
CREATE TABLE IF NOT EXISTS users_films_like (
    user_id BIGINT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    film_id BIGINT NOT NULL REFERENCES films (film_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, film_id)
);

-- Удаляется пользователь - удаляется 2 записи в таблице (если дружба авторизована)
-- 1 запись - если дружба не авторизована
CREATE TABLE IF NOT EXISTS friendship (
    user_id BIGINT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    friend_id BIGINT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    film_id BIGINT NOT NULL REFERENCES films (film_id) ON DELETE CASCADE,
    is_positive BOOL,
    content VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS reviews_likes (
    review_id BIGINT NOT NULL REFERENCES reviews (id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    liked BIT,
    PRIMARY KEY (review_id, user_id)
);