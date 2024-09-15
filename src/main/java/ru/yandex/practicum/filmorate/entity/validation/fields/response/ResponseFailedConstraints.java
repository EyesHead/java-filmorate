package ru.yandex.practicum.filmorate.entity.validation.fields.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ResponseFailedConstraints {
    private final List<FieldError> fieldErrors;
}
