package ru.yandex.practicum.filmorate.model.responses.error;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class ErrorMessage {
    private final String message;
}
