package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.entity.User;
import ru.yandex.practicum.filmorate.service.UserFriendsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{id}/friends")
public class UserFriendsController {
    private final UserFriendsService userFriendsService;

    @PutMapping("/{friendId}")
    public void addFriend(
            @PathVariable(name = "id") @NotNull Long userId,
            @PathVariable(name = "friendId") @NotNull Long friendId) {
        userFriendsService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{friendId}")
    public void deleteFriend(
            @PathVariable(name = "id") @NotNull Long userId,
            @PathVariable(name = "friendId") @NotNull Long friendId) {
        userFriendsService.removeFriend(userId, friendId);
    }

    @GetMapping
    public List<User> getUserFriends(@PathVariable(name = "id") @NotNull Long userId) {
        return userFriendsService.getUserFriends(userId);
    }

    @GetMapping("/common/{friendId}")
    public List<User> getCommonFriends(
            @PathVariable(name = "id") @NotNull Long userId,
            @PathVariable(name = "friendId") @NotNull Long friendId) {
        return userFriendsService.getCommonFriends(userId, friendId);
    }
}
