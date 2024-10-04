package ru.yandex.practicum.filmorate.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.*;
import ru.yandex.practicum.filmorate.validation.constraints.FilmReleaseDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class Film {
    @NotNull(message = "Id is required for update",
            groups = {Marker.OnUpdate.class})
    Long id;

    @NotBlank(message = "Film name is required",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    String name;

    @Size(max = 200, message = "Film description should be less than 200 characters",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    String description;

    @FilmReleaseDate(message = "Film release date cannot be date before first film was made (12/28/1895)",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    LocalDate releaseDate;

    @Positive(message = "Film duration is positive number",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    Integer duration;

    Mpa mpa;

    @Builder.Default
    Set<Genre> genres = new HashSet<>();

    @Builder.Default
    Set<Director> directors = new HashSet<>();

    @Builder.Default
    List<Long> userIdsWhoLiked = new ArrayList<>();

    public Set<Genre> getGenres() {
        return (this.genres == null) ? new HashSet<>() : this.genres;
    }

    public Set<Director> getDirectors() {
        return (this.directors == null) ? new HashSet<>() : this.directors;
    }
}