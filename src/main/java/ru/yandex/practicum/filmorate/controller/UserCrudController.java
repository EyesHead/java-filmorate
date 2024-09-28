package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.entity.Marker;
import ru.yandex.practicum.filmorate.entity.User;
import ru.yandex.practicum.filmorate.service.UserCrudService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserCrudController {
    private final UserCrudService userService;

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
        return userService.createUser(user);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping
    public User updateUser(@RequestBody
                           @Validated(Marker.OnUpdate.class)
                           User user) {
        return userService.updateUser(user);
    }

    @GetMapping("{userId}")
    @ResponseStatus(HttpStatus.OK)
    public User getUserById(@NotNull
                            @Positive
                            @PathVariable
                            long userId) {
        return userService.getUserById(userId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable long userId) {
        userService.deleteUserById(userId);
    }
}
