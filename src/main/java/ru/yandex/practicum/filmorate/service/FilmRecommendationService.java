package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.entity.Film;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.LikeStorage;
import ru.yandex.practicum.filmorate.service.validators.UserValidator;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmRecommendationService {
    private final LikeStorage likeStorage;
    private final UserValidator userValidator;
    private final FilmStorage filmStorage;

    public List<Film> getRecommendedFilms(Long userId) {
        userValidator.checkUserOnExist(userId);

        log.info("(NEW) Получение списка рекомендованных фильмов для пользователя с id = {}", userId);

        List<Long> recommendedFilmsIds = getListOfFilmsToRecommend(userId);

        List<Film> recommendedFilms = filmStorage.getListOfFilmsById(recommendedFilmsIds);

        log.info("(END) Возвращено {} рекомендованных фильмов для пользователя с id = {}", recommendedFilms.size(), userId);

        return recommendedFilms;
    }

    private List<Long> getUsersWithSimilarLikesToFilms(List<Long> likedFilmsId, Long userId) {
        log.debug("(Service) Получение списка пользователей с похожими лайками для пользователя с id = {}", userId);

        Map<Long, ArrayList<Long>> mapOfFilmsToUserLists = likeStorage.getMapOfLikesByPrimaryKey(likedFilmsId, "film_id");
        List<Long> listOfSimilarLikes = new ArrayList<>();

        for (Map.Entry<Long, ArrayList<Long>> filmLikes : mapOfFilmsToUserLists.entrySet()) {
            listOfSimilarLikes.addAll(filmLikes.getValue());
        }

        Map<Long, Long> counter = listOfSimilarLikes.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        int amountOfUsers = 1;
        List<Long> similarUsers = counter.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(userId))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(amountOfUsers)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.debug("(Service) Найдены пользователи с похожими лайками: {}", similarUsers);
        return similarUsers;
    }

    private List<Long> getListOfFilmsToRecommend(Long userId) {
        log.debug("(Service) Получение списка фильмов для рекомендации для пользователя с id = {}", userId);

        List<Long> likedFilmsId = likeStorage.getMapOfLikesByPrimaryKey(Collections.singletonList(userId), "user_id")
                .get(userId);

        log.debug("(Service) Пользователь с id = {} лайкнул фильмы: {}", userId, likedFilmsId);

        List<Long> similarUsers = getUsersWithSimilarLikesToFilms(likedFilmsId, userId);

        Map<Long, ArrayList<Long>> similarUsersLikes = likeStorage.getMapOfLikesByPrimaryKey(similarUsers, "user_id");

        List<Long> recommendedFilmsIds = similarUsersLikes.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet()).stream()
                .filter(filmId -> !likedFilmsId.contains(filmId))
                .collect(Collectors.toList());

        log.debug("(Service) Список рекомендованных фильмов (id): {}", recommendedFilmsIds);
        return recommendedFilmsIds;
    }
}