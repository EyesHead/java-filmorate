package ru.yandex.practicum.filmorate.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.validation.constraints.FilmReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true, builderClassName = "FilmBuilder")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Film {
    @NotNull(message = "Значение id обязательно должно быть указано для обновления фильма",
            groups = {Marker.OnUpdate.class})
    Long id;

    @NotBlank(message = "Название фильма не должно состоять из пробельных символов, быть пустым или отсутствовать",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    String name;

    @Size(max = 200, message = "Описание должно состоять не более чем из 200 символов",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    String description;

    @FilmReleaseDate(message = "Дата релиза должна быть позже, чем дата выпуска первого фильма (12/28/1895)",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    LocalDate releaseDate;

    @Positive(message = "Длительность фильма должна быть положительным числом",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    Integer duration;

    Set<Long> usersLikes;

    public Set<Long> getUsersLikes() {
        if (usersLikes == null) {
            return new HashSet<>();
        }
        return usersLikes;
    }
}
