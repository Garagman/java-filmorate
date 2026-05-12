package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmServiceTest {

    private FilmService filmService;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;

    private final MpaStorage mpaStorage = new MpaStorage() {
        @Override
        public List<Mpa> getAll() {
            return List.of(createMpa(1, "G"), createMpa(2, "PG"), createMpa(3, "PG-13"),
                    createMpa(4, "R"), createMpa(5, "NC-17"));
        }

        @Override
        public Mpa getById(Integer id) {
            return getAll().stream()
                    .filter(m -> m.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("MPA not found"));
        }

        private Mpa createMpa(Integer id, String name) {
            Mpa m = new Mpa();
            m.setId(id);
            m.setName(name);
            return m;
        }
    };

    private final GenreStorage genreStorage = new GenreStorage() {
        @Override
        public List<Genre> getAll() {
            return List.of(createGenre(1, "Комедия"), createGenre(2, "Драма"), createGenre(3, "Мультфильм"),
                    createGenre(4, "Триллер"), createGenre(5, "Документальный"), createGenre(6, "Боевик"));
        }

        @Override
        public Genre getById(Integer id) {
            return getAll().stream()
                    .filter(g -> g.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Genre not found"));
        }

        private Genre createGenre(Integer id, String name) {
            Genre g = new Genre();
            g.setId(id);
            g.setName(name);
            return g;
        }
    };

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage, mpaStorage, genreStorage);
    }

    @Test
    void shouldCreateValidFilm() {
        Film film = validFilm();
        film.setMpaId(1);
        film.setGenreIds(Set.of(1, 2));

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
    void shouldRejectTooLongDescription() {
        Film film = validFilm();
        film.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> filmService.createFilm(film));
    }

    @Test
    void shouldRejectInvalidReleaseDate() {
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

    @Test
    void shouldRejectInvalidMpaId() {
        Film film = validFilm();
        film.setMpaId(999);
        assertThrows(ValidationException.class, () -> filmService.createFilm(film));
    }

    @Test
    void shouldRejectInvalidGenreId() {
        Film film = validFilm();
        film.setGenreIds(Set.of(999));
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