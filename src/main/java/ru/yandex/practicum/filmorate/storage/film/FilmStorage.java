package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;

public interface FilmStorage {
    Film create(Film film);
    Film update(Film film);
    Film getById(Integer id);
    List<Film> getAll();
    
    List<Film> getPopular(int count);
    void addLike(Integer filmId, Integer userId);
    void removeLike(Integer filmId, Integer userId);
}