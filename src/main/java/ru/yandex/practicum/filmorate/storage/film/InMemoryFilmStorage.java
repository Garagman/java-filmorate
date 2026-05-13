package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public Film create(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null || !films.containsKey(film.getId())) {
            throw new NotFoundException(
                    String.format("Фильм с id=%s не найден", film.getId())
            );
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film getById(Integer id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException(String.format("Фильм с id=%d не найден", id));
        }
        return film;
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public List<Film> getPopular(int count) {
        throw new UnsupportedOperationException("getPopular не поддерживается в InMemory-режиме");
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        throw new UnsupportedOperationException("addLike не поддерживается в InMemory-режиме");
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        throw new UnsupportedOperationException("removeLike не поддерживается в InMemory-режиме");
    }
}