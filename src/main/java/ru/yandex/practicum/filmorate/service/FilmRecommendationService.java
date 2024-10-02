package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.LikeStorage;
import ru.yandex.practicum.filmorate.service.validators.UserValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmRecommendationService {
    private final LikeStorage likeStorage;
    private final UserValidator userValidator;
    private final FilmStorage filmStorage;

    private List<Long> getAnotherUsersWithSimilarLikes(List<Long> likedFilmsId, Long userId) {
        log.info("Получение списка пользователей с похожими лайками");
        Map<Long, ArrayList<Long>> mapOfFilmsToUserLists = likeStorage.getMapOfLikesByPrimaryKey(likedFilmsId,
                "film_id");
        List<Long> listOfSimilarLikes = new ArrayList<>();
        for (Map.Entry<Long, ArrayList<Long>> filmLikes : mapOfFilmsToUserLists.entrySet()) {
            listOfSimilarLikes.addAll(filmLikes.getValue());
        }
        Map<Long, Long> counter = listOfSimilarLikes.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        int amountOfUsers = 1;
        return counter.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(userId))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(amountOfUsers)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<Long> getListOfFilmsToRecommend(Long userId) {
        log.info("Получение списка ID фильмов для рекомендации пользователю {}", userId);
        List<Long> likedFilmsId = likeStorage.getMapOfLikesByPrimaryKey(Collections.singletonList(userId),
                "user_id").get(userId);
        List<Long> similarUsers = getAnotherUsersWithSimilarLikes(likedFilmsId, userId);
        log.info("Пользователи с похожими лайками {}", similarUsers);
        Map<Long, ArrayList<Long>> similarUsersLikes = likeStorage.getMapOfLikesByPrimaryKey(similarUsers,
                "user_id");
        return similarUsersLikes.values().stream().flatMap(List::stream).collect(Collectors.toSet()).stream()
                .filter(filmId -> !likedFilmsId.contains(filmId)).collect(Collectors.toList());
    }

    public List<Film> getRecommendedFilms(Long userId) {
        userValidator.checkUserOnExist(userId);
        List<Long> recommendedFilmsIds = getListOfFilmsToRecommend(userId);
        log.info("Получение списка фильмов по id {}", recommendedFilmsIds);
        return filmStorage.getListOfFilmsById(recommendedFilmsIds);
    }
}
