package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    Integer id;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;

    Set<Genre> genres = new HashSet<>();
    Mpa mpa;

    Set<Integer> genreIds = new HashSet<>();
    Integer mpaId;

    Set<Integer> likes = new HashSet<>();
}