package ru.yandex.practicum.filmorate.serviceRepo;

import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public interface FilmorateRepository<T> {
    Collection<T> getAll();

    T create(T entity);

    T update(T entity);
}
