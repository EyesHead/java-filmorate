package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.entity.Director;
import ru.yandex.practicum.filmorate.entity.Marker;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;
import ru.yandex.practicum.filmorate.service.util.DirectorValidator;

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

        return directorStorage.getDirectorById(directorId).orElseThrow(
                () -> new NotFoundException("Режиссер не найден. Id = " + directorId)
        );
    }

    public Director createDirector(@Validated(Marker.OnCreate.class) Director director) {
        log.info("(NEW) Получен запрос на создание режиссера '{}'", director.getName());

        return directorStorage.saveDirector(director);
    }

    public List<Director> getAllDirectors() {
        log.info("(NEW) Получен запрос на получение списка всех рижессеров");

        return directorStorage.getAllDirectors();
    }

    public void removeDirector(long directorId) {
        log.info("(NEW) Получен запрос на удаление режиссера с id = '{}'", directorId);

        directorStorage.removeDirector(directorId);
    }

    public Director updateDirector(Director director) {
        log.info("(NEW) Получен запрос на обновление режиссера с id = '{}'", director.getId());

        directorValidator.checkDirectorOnExists(director.getId());
        return directorStorage.updateDirector(director);
    }
}
