package ru.yandex.practicum.filmorate.controller.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.validation.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.validation.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.responses.error.ErrorMessage;
import ru.yandex.practicum.filmorate.model.responses.error.ResponseFailedConstraints;
import ru.yandex.practicum.filmorate.model.responses.error.FieldError;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseFailedConstraints handleAnnotationConstraints(MethodArgumentNotValidException e) {
        final List<FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> {
                    String failedFieldName = fieldError.getField();
                    String errorMessage = fieldError.getDefaultMessage();

                    log.error("Пользователь неверно указал значение для поля {}. {}", failedFieldName, errorMessage);
                    return new FieldError(failedFieldName, errorMessage);
                })
                .toList();
        return new ResponseFailedConstraints(fieldErrors);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleFailedValidations(ValidationException exception) {
        log.error(exception.getMessage());
        return new ErrorMessage(exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleNotFoundException(NotFoundException exception) {
        log.error(exception.getMessage());
        return new ErrorMessage(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleUnresolvedException(Throwable throwable) {
        log.error(throwable.getMessage());
        return new ErrorMessage("Во время выполнения запроса возникла непредвиденная ошибка");
    }
}
