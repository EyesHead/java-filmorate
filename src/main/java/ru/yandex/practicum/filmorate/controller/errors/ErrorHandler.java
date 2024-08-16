package ru.yandex.practicum.filmorate.controller.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.validation.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.validation.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.responses.error.ErrorMessage;
import ru.yandex.practicum.filmorate.model.responses.error.FailedConstraintsResponse;
import ru.yandex.practicum.filmorate.model.responses.error.Violation;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
     public FailedConstraintsResponse handleFieldOfMethodArgumentConstraints(MethodArgumentNotValidException e) {
        final List<Violation> violations = e.getBindingResult().getFieldErrors().stream()
                .map(violation -> {
                    log.warn("Пользователь неверно указал значение для поля {}. {}",
                            violation.getField(), violation.getDefaultMessage());
                    return new Violation(
                            violation.getField(), // имя поля, где произошла ошибка
                            violation.getDefaultMessage());
                }) // сообщение об ошибке
                .toList();
        return new FailedConstraintsResponse(violations);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleServiceValidationException(ValidationException exception) {
        log.warn(exception.getMessage());
        return new ErrorMessage(exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleNotFoundException(NotFoundException exception) {
        log.warn(exception.getMessage());
        return new ErrorMessage(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleUnresolvedException(Throwable throwable) {
        log.error(throwable.getMessage());
        return new ErrorMessage("Во время выполнения запроса возникла непредвиденная ошибка");
    }
}
