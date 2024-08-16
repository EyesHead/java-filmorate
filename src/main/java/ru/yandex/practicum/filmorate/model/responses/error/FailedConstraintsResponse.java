package ru.yandex.practicum.filmorate.model.responses.error;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class FailedConstraintsResponse {
    private final List<Violation> violations;
}
