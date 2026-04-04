package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmServiceTest {
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService();
    }

    @Test
    void shouldCreateValidFilm() {
        Film film = validFilm();

        Film created = filmService.createFilm(film);

        assertEquals(1, created.getId());
        assertEquals("Film", created.getName());
    }

    @Test
    void shouldRejectEmptyName() {
        Film film = validFilm();
        film.setName(" ");

        assertThrows(ValidationException.class, () -> filmService.createFilm(film));
    }

    @Test
    public void shouldRejectTooLongDescription() {
        Film film = validFilm();
        film.setDescription("a".repeat(201));

        assertThrows(ValidationException.class, () -> filmService.createFilm(film));
    }

    @Test
    public void shouldRejectInvalidReleaseDate() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> filmService.createFilm(film));
    }

    @Test
    void shouldRejectInvalidDuration() {
        Film film = validFilm();
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> filmService.createFilm(film));
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }
}