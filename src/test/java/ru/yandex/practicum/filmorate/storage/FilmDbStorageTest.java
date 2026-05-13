package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.storage.mapper.MpaMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmMapper.class, GenreMapper.class, MpaMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateFilmWithGenresAndMpa() {
        Film film = createTestFilm();
        film.setMpaId(1); // G
        film.setGenreIds(Set.of(1, 2)); // Комедия, Драма

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getMpa().getName()).isEqualTo("G");
        assertThat(created.getGenres()).hasSize(2);
    }

    @Test
    void shouldFindFilmById() {
        Film created = filmStorage.create(createTestFilm());
        Film found = filmStorage.getById(created.getId());

        assertThat(found.getName()).isEqualTo("Test Film");
        assertThat(found.getDuration()).isEqualTo(120);
    }

    @Test
    void shouldThrowNotFoundWhenFilmNotExist() {
        assertThatThrownBy(() -> filmStorage.getById(9999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldUpdateFilm() {
        Film film = filmStorage.create(createTestFilm());
        film.setName("Updated Film");
        film.setGenreIds(Set.of(3)); // Обновляем жанры
        filmStorage.update(film);

        Film updated = filmStorage.getById(film.getId());
        assertThat(updated.getName()).isEqualTo("Updated Film");
        assertThat(updated.getGenres()).hasSize(1);
    }

    @Test
    void shouldGetAllFilms() {
        filmStorage.create(createTestFilm());
        filmStorage.create(createTestFilm2());

        List<Film> films = filmStorage.getAll();
        assertThat(films).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldGetPopularFilms() {
        Film f1 = filmStorage.create(createTestFilm());
        Film f2 = filmStorage.create(createTestFilm2());

        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                1, "user1@test.com", "user1", "User1", LocalDate.of(1990, 1, 1)
        );
        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                2, "user2@test.com", "user2", "User2", LocalDate.of(1991, 1, 1)
        );
        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                3, "user3@test.com", "user3", "User3", LocalDate.of(1992, 1, 1)
        );

        filmStorage.addLike(f1.getId(), 1);
        filmStorage.addLike(f1.getId(), 2);
        filmStorage.addLike(f2.getId(), 3);

        List<Film> popular = filmStorage.getPopular(1);
        assertThat(popular).hasSize(1);
        assertThat(popular.get(0).getId()).isEqualTo(f1.getId());
        assertThat(popular.get(0).getName()).isEqualTo("Test Film");
    }

    private Film createTestFilm() {
        Film f = new Film();
        f.setName("Test Film");
        f.setDescription("Desc");
        f.setReleaseDate(LocalDate.of(2020, 1, 1));
        f.setDuration(120);
        return f;
    }

    private Film createTestFilm2() {
        Film f = createTestFilm();
        f.setName("Test Film 2");
        f.setDuration(90);
        return f;
    }
}