package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InvalidDataRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.entity.validation.fields.response.ErrorMessage;
import ru.yandex.practicum.filmorate.entity.validation.fields.response.ResponseFailedConstraints;
import ru.yandex.practicum.filmorate.entity.validation.fields.response.FieldError;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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
        log.warn("{}", fieldErrors);
        return new ResponseFailedConstraints(fieldErrors);
    }

    @ExceptionHandler(DuplicatedDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleFailedValidations(DuplicatedDataException exception) {
        log.warn(exception.getMessage());
        return new ErrorMessage(exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleNotFoundException(NotFoundException exception) {
        log.warn(exception.getMessage());
        return new ErrorMessage(exception.getMessage());
    }

    @ExceptionHandler(InvalidDataRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleInvalidRequestDataException(InvalidDataRequestException e) {
        log.warn(e.getMessage());
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleEmptyResultDataAccessException(EmptyResultDataAccessException exception) {
        log.warn("Результат не найден: {}", exception.getMessage());
        return new ErrorMessage("Ресурс не найден.");
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleDataAccessException(DataAccessException dae) {
        log.error("Ошибка доступа к данным: {}", dae.getMessage());
        return new ErrorMessage("Ошибка доступа к данным.");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleUnresolvedException(Throwable throwable) {
        log.error("Сообщение из ошибки = {}", throwable.getMessage());
        return new ErrorMessage("Во время выполнения запроса возникла непредвиденная ошибка");
    }
}