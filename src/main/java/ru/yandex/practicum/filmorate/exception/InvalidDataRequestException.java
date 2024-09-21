package ru.yandex.practicum.filmorate.exception;

public class InvalidDataRequestException extends RuntimeException {
    public InvalidDataRequestException(String message) {
        super(message);
    }
}
