# Backend для сервиса по поиску и оценки фильма

### Краткое описание

Приложение хранит фильмы в базе данных. Имеет функции поиска фильма по подстроке названия фильма и имени режиссера.
Выводит топ фильмов. Показывает рекомендации на основании оценок друзей. Имеет возможность добавлять и удалять друзей.
Включает в себя функции оставлять отзывы к фильмам, оценивать фильмы и отзывы других пользователей.

### Используемые технологии:

- SpringBoot
- Lombok
- Maven
- JDBC
- Git
- Postman
- REST API

## Функциональность

### 1.Фильмы

- Добавление фильма: *POST /films*
  Тело запроса:

```
{
  "name": "Kill Bill",
  "description": "And the bride came",
  "releaseDate": "2003-09-29",
  "duration": 111,
  "mpa": { "id": 4},
  "genres": [ {"id": 4},{"id": 6} ],
  "directors" : [ {"id": 2} ]
}
```

Тело ответа:

```
{
    "id": 1,
    "name": "Kill Bill",
    "description": "And the bride came",
    "releaseDate": "2003-09-29",
    "duration": 111,
    "mpa": {
        "id": 4,
        "name": "R"
    },
    "genres": [
        {
            "id": 4,
            "name": "Триллер"
        },
        {
            "id": 6,
            "name": "Боевик"
        }
    ],
    "directors": [
        {
            "id": 1,
            "name": "Quentin Tarantino"
        }
    ],
    "userIdsWhoLiked": null
}
```

- Обновление фильма: *PUT /films*
- Удаление фильма по id: *DELETE /films/{id}*
- Получение фильма по id:  *GET /films/{id}*
- Получение всех фильмов:  *GET /films*

### 2.Пользователи

- Добавление пользователя: *POST /users*
- Обновление пользователя: *PUT /users*
- Удаление пользователя по id: *DELETE /users/{id}*
- Получение всех пользователей:  *GET /users*

### 3.Друзья

- Добавить в друзья: *PUT /users/{id}/friends/{friendId}*
- Удалить из друзей: *DELETE /users/{id}/friends/{friendId}*
- Общие друзья: *GET /users/{id}/friends/common/{otherId}*

### 4.Лайки

- Добавить лайк фильму: *PUT /films/{id}/like/{userId}*
- Удалить лайк у фильма: *DELETE /films/{id}/like/{userId}*
- Получить топ фильмов: *GET /films/popular?count={count}*

### 5. Рекомендации

- Получить фильмы для пользователя по id: *GET /users/{userId}/recommendations*

### 6. Поиск

- Получить фильмы по подстроке: *GET /films/search?query={query}=title,director*
- Получить общие с другом фильмы: *GET /films/common?userId={userId}&friendId={friendId}*
- Получить популярные фильмы по жанру и году *GET /films/popular?count={limit}&genreId={genreId}&year={year}*

### 7. Отзывы

- Добавить отзыв: *POST /reviews*

Тело запроса

```
{
  "content": "This film is soo bad.",
  "isPositive": false,
  "userId": 1,
  "filmId": 1
}
```

- Обновить отзыв *PUT /reviews*

  Тело запроса

```
{
  "reviewId": 1,
  "content": "This film is not too bad.",
  "isPositive": true,
  "userId": 2,
  "filmId": 2,
  "useful": 10
}
```

- Удалить отзыв *PUT /reviews/{reviewId}*
- Пользователь ставит лайк отзыву: *PUT /reviews/{id}/like/{userId}*
- Пользователь ставит дизлайк отзыву: *PUT /reviews/{id}/dislike/{userId}*
- Пользователь удаляет лайк/дизлайк отзыву: *DELETE /reviews/{id}/like/{userId}*
- Пользователь удаляет дизлайк отзыву: *DELETE /reviews/{id}/dislike/{userId}*

### 8. Лента событий

- Получить ленту событий пользователя: *GET /users/{id}/feed*

  Пример ответа:

```
[
    {
        "timestamp": 123344556,
        "userId": 1,
        "eventType": "LIKE", // одно из значениий LIKE, REVIEW или FRIEND
			  "operation": "REMOVE", // одно из значениий REMOVE, ADD, UPDATE
        "eventId": 3, //primary key
        "entityId": 5   // идентификатор сущности, с которой произошло событие
    }
]
```

### 9. Жанры

- Получить все жанры: *GET /genres*
- Получить жанр по id: *GET /genres/{id}*

### 10. Рейтинги

- Получить все рейтинги: *GET /mpa*
- Получить рейтинг по id: *GET /mpa/{id}*

### 11. Режиссеры

- Создание режиссёра: *POST /directors*

  Тело запроса:
```
{
"name": "Quentin Tarantino"
}

```
- Изменение режиссёра: *PUT /directors*
- Получение режиссёра по id: *GET /directors/{id}*
- Список всех режиссёров: *GET /directors*
- Удаление режиссёра *DELETE /directors/{id}*


##  ER diagram:

https://miro.com/app/board/uXjVLZ8kfXY=/
