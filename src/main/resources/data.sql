-- Очистка таблиц перед вставкой данных
DELETE FROM friendship;
DELETE FROM users_films_like;
DELETE FROM films_genres;
DELETE FROM films;
DELETE FROM users;
DELETE FROM mpa;
DELETE FROM genres;

-- Сброс AUTO_INCREMENT для всех таблиц
ALTER TABLE friendship ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE friendship ALTER COLUMN friend_id RESTART WITH 1;

ALTER TABLE users_films_like ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE users_films_like ALTER COLUMN film_id RESTART WITH 1;

ALTER TABLE films_genres ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE films_genres ALTER COLUMN genre_id RESTART WITH 1;

ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE mpa ALTER COLUMN mpa_id RESTART WITH 1;
ALTER TABLE genres ALTER COLUMN genre_id RESTART WITH 1;

INSERT INTO mpa VALUES (1, 'G'),
                       (2, 'PG'),
                       (3, 'PG-13'),
                       (4, 'R'),
                       (5, 'NC-17');

INSERT INTO genres VALUES (1, 'Комедия'),
                          (2, 'Драма'),
                          (3, 'Мультфильм'),
                          (4, 'Триллер'),
                          (5, 'Документальный'),
                          (6, 'Боевик');
