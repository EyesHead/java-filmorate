package ru.yandex.practicum.filmorate.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.constraints.NoSpaces;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true, builderClassName = "UserBuilder")
public class User {
    @NotNull(message = "Значение id обязательно должно быть указано для обновления пользователя",
            groups = {Marker.OnUpdate.class})
    private final Long id;

    @NotBlank(groups = {Marker.OnCreate.class})
    @Email(message = "Неверный формат записи электронной почты",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private final String email;

    @NotBlank(groups = Marker.OnCreate.class)
    @NoSpaces(message = "Логин не должен содержать пробельных символов",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private final String login;

    private final String name;

    @Past(message = "День рождения не может быть датой из будущего",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private final LocalDate birthday;

    @Builder.Default
    private final Set<Long> friendsIds = new HashSet<>();
}
