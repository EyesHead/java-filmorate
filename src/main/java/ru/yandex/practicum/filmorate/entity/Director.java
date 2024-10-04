package ru.yandex.practicum.filmorate.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class Director {
    @NotNull(message = "Id is required for update", groups = {Marker.OnUpdate.class})
    Long id;

    @NotBlank(message = "Director name is required", groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    String name;
}