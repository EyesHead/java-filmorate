package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.entity.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    Optional<Director> getDirectorById(long directorId);

    Director saveDirector(Director director);

    List<Director> getAllDirectors();

    void removeDirector(long directorId);

    Director updateDirector(Director director);
}
