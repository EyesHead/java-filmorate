package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.serviceRepo.FilmorateRepository;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j(topic = "Users controller log")
@Validated
public class UserController {
    FilmorateRepository<User> userService;

    @Autowired
    public UserController(FilmorateRepository<User> userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("GET request by client received");
        return userService.getAll();
    }

    @PostMapping
    public User createNewUser(@Valid @RequestBody User user) {
        log.info("POST request by client received");
        return userService.create(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("PUT request by client received");
        return userService.update(user);
    }
}
