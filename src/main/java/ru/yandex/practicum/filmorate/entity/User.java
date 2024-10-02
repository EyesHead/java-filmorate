package ru.yandex.practicum.filmorate.entity;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.validation.constraints.LoginForm;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @NotNull(groups = {Marker.OnUpdate.class})
    @Positive
    final Long id;

    @NotBlank(groups = Marker.OnCreate.class)
    @Email(groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    final String email;

    @NotBlank(groups = Marker.OnCreate.class)
    @LoginForm(groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    final String login;

    @Setter
    String name;

    @PastOrPresent(groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    final LocalDate birthday;

    @Builder.Default
    final List<Long> friendsIds = new ArrayList<>();
}
