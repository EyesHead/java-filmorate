package ru.yandex.practicum.filmorate.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class Event {
    @JsonProperty("eventId")
    Long id;
    Long userId;
    EventType eventType;
    EventOperation operation;
    @JsonProperty("timestamp")
    long epochMilli;
    long entityId;
}