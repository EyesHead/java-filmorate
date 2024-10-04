package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Director;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;
import ru.yandex.practicum.filmorate.service.validators.DirectorValidator;

import java.util.List;

@Service
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class DirectorService {
    DirectorStorage directorStorage;
    DirectorValidator directorValidator;

    public Director getDirectorById(long directorId) {
        log.info("(NEW) Получен запрос на получение режиссера по id = '{}'", directorId);

        Director foundDirector = directorStorage.getDirectorById(directorId).orElseThrow(
                () -> new NotFoundException("Режиссер не найден. Id = " + directorId)
        );
        log.info("(END) Режиссёр с id = '{}' был найден и получен: {}", directorId, foundDirector);
        return foundDirector;
    }

    public Director createDirector(Director director) {
        log.info("(NEW) Получен запрос на создание режиссера '{}'", director.getName());

        Director createdDirector = directorStorage.saveDirector(director);
        log.info("(END) Режиссёр '{}' был успешно создан: {}", director.getName(), createdDirector);
        return createdDirector;
    }

    public List<Director> getAllDirectors() {
        log.info("(NEW) Получен запрос на получение списка всех режиссеров");

        List<Director> directors = directorStorage.getAllDirectors();
        log.info("(END) Список всех режиссеров был успешно получен. Количество: {}", directors.size());
        return directors;
    }

    public void removeDirector(long directorId) {
        log.info("(NEW) Получен запрос на удаление режиссера с id = '{}'", directorId);

        directorValidator.checkDirectorOnExists(directorId);

        directorStorage.removeDirector(directorId);
        log.info("(END) Режиссёр с id = '{}' был успешно удалён", directorId);
    }

    public Director updateDirector(Director director) {
        log.info("(NEW) Получен запрос на обновление режиссера с id = '{}'", director.getId());

        directorValidator.checkDirectorOnExists(director.getId());

        Director updatedDirector = directorStorage.updateDirector(director);
        log.info("(END) Режиссёр с id = '{}' был успешно обновлён: {}", director.getId(), updatedDirector);
        return updatedDirector;
    }
}