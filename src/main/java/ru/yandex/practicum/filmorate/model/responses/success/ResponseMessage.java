package ru.yandex.practicum.filmorate.model.responses.success;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ResponseMessage {
    private final String successMessage;
}
