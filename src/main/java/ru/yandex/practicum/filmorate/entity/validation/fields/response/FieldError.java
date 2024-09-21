package ru.yandex.practicum.filmorate.entity.validation.fields.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FieldError {
    private final String failedFieldName;
    private final String errorMessage;
}