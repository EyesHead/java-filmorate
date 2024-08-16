package ru.yandex.practicum.filmorate.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.dto.User;
import ru.yandex.practicum.filmorate.model.responses.success.ResponseMessage;
import ru.yandex.practicum.filmorate.service.user.UserFriendsService;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{id}/friends")
public class UserFriendsController {
    private final UserFriendsService userFriendsService;

    @PutMapping("/{friendId}")
    public ResponseMessage addFriend(
            @PathVariable(name = "id") Long userId,
            @PathVariable(name = "friendId") Long friendId) {
        return userFriendsService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{friendId}")
    public ResponseMessage deleteFriend(
            @PathVariable(name = "id") Long userId,
            @PathVariable(name = "friendId") Long friendId) {
        return userFriendsService.removeFriend(userId, friendId);
    }

    @GetMapping
    public List<User> getAllFriends(@PathVariable(name = "id") Long userId) {
        return userFriendsService.getAllFriends(userId);
    }

    @GetMapping("/common/{friendId}")
    public Set<User> getCommonFriends(
            @PathVariable(name = "id") Long userId,
            @PathVariable(name = "friendId") Long friendId) {
        return userFriendsService.getCommonFriends(userId, friendId);
    }
}