package ru.yandex.practicum.filmorate.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.dto.Marker;
import ru.yandex.practicum.filmorate.model.dto.User;
import ru.yandex.practicum.filmorate.service.user.CrudUserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final CrudUserService userService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Collection<User> getAllUsers() {
        return userService.getAll();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public User createNewUser(@RequestBody
                              @Validated(Marker.OnCreate.class)
                              User user) {
        return userService.create(user);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping
    public User updateUser(@RequestBody
                           @Validated(Marker.OnUpdate.class)
                           User user) {
        return userService.update(user);
    }
}
