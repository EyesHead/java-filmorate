package ru.yandex.practicum.filmorate.entity.validation.fields.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class ErrorMessage {
    private final String error;
}
