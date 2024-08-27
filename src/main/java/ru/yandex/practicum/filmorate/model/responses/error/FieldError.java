package ru.yandex.practicum.filmorate.model.responses.error;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FieldError {
    private final String failedFieldName;
    private final String errorMessage;
}