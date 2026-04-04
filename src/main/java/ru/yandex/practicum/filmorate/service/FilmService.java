package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FilmService {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final Map<Integer, Film> films = new LinkedHashMap<>();
    private int nextId = 1;

    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    public Film createFilm(Film film) {
        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    public Film updateFilm(Film film) {
        validateFilm(film);

        if (film.getId() == null || film.getId() <= 0) {
            log.warn("Ошибка валидации фильма: некорректный id");
            throw new ValidationException("Id фильма должен быть положительным числом");
        }

        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        log.info("Обновлен фильм: {}", film);
        return film;
    }

    private void validateFilm(Film film) {
        if (film == null) {
            log.warn("Ошибка валидации фильма: тело запроса пустое");
            throw new ValidationException("Тело запроса не должно быть пустым");
        }

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации фильма: пустое название");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Ошибка валидации фильма: описание длиннее 200 символов");
            throw new ValidationException("Максимальная длина описания фильма — 200 символов");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Ошибка валидации фильма: некорректная дата релиза");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Ошибка валидации фильма: некорректная длительность");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}