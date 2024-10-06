package ru.yandex.practicum.filmorate.service.validators;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DirectorValidator {
    DirectorStorage directorStorage;

    public void checkDirectorOnExists(long directorId) throws NotFoundException {
        directorStorage.getDirectorById(directorId).orElseThrow(
                () -> new NotFoundException("(END) Director was not found. Id = " + directorId));
    }
}
