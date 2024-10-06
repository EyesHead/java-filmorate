DROP TABLE IF EXISTS mpa, users, films, genres, directors, films_genres, users_films_like, friendship,
    reviews, reviews_likes, films_directors, events;

-- Таблица mpa
CREATE TABLE IF NOT EXISTS mpa (
    mpa_id BIGSERIAL PRIMARY KEY,
    mpa_name VARCHAR(10) NOT NULL UNIQUE
);

-- Таблица users
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    login VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(150),
    birthday DATE
);

-- Таблица films
CREATE TABLE IF NOT EXISTS films (
    film_id BIGSERIAL PRIMARY KEY,
    film_name VARCHAR(150) NOT NULL,
    description VARCHAR(200),
    release_date DATE,
    duration INTEGER CHECK(duration > 0),
    mpa_id BIGINT REFERENCES mpa(mpa_id)
);

-- Таблица всех жанров. Названия жанров уникальны
CREATE TABLE IF NOT EXISTS genres (
    genre_id SERIAL PRIMARY KEY,
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

-- Таблица режиссеров
CREATE TABLE IF NOT EXISTS directors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL
);

-- Таблица связи many-to-many между фильмами и режиссерами
CREATE TABLE IF NOT EXISTS films_directors (
    film_id BIGINT NOT NULL REFERENCES films (film_id) ON DELETE CASCADE,
    director_id BIGINT NOT NULL REFERENCES directors (id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, director_id)
);

-- Таблица отзывов
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    film_id BIGINT NOT NULL REFERENCES films (film_id) ON DELETE CASCADE,
    is_positive BOOLEAN,
    content VARCHAR(200)
);

-- Таблица лайков для отзывов
CREATE TABLE IF NOT EXISTS reviews_likes (
    review_id BIGINT NOT NULL REFERENCES reviews (id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    liked BOOLEAN,
    PRIMARY KEY (review_id, user_id)
);

-- Таблица событий
CREATE TABLE IF NOT EXISTS events (
    user_id BIGINT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    type_id INTEGER NOT NULL,
    operation_id INTEGER NOT NULL,
    entity_id BIGINT NOT NULL,
    event_timestamp BIGINT NOT NULL
);