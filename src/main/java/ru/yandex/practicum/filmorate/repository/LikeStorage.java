package ru.yandex.practicum.filmorate.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface LikeStorage {
    Map<Long, ArrayList<Long>> getMapOfLikesByPrimaryKey(List<Long> listOfIds, String primaryKey);
}
