package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.storage.mapper.MpaMapper;

import java.sql.PreparedStatement;
import java.util.*;

@Repository
@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;
    private final GenreMapper genreMapper;
    private final MpaMapper mpaMapper;

    private static final String SQL_INSERT =
            "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM films WHERE id = ?";

    private static final String SQL_FIND_ALL =
            "SELECT * FROM films";

    private static final String SQL_FIND_POPULAR =
            "SELECT f.*, COUNT(l.user_id) as likes_count FROM films f " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "GROUP BY f.id ORDER BY likes_count DESC LIMIT ?";

    private static final String SQL_ADD_LIKE =
            "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";

    private static final String SQL_REMOVE_LIKE =
            "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String SQL_FIND_GENRES_BY_FILM_ID =
            "SELECT g.* FROM genres g " +
                    "JOIN film_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id = ?";

    private static final String SQL_FIND_MPA_BY_ID =
            "SELECT * FROM mpa WHERE id = ?";

    private static final String SQL_INSERT_FILM_GENRE =
            "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

    private static final String SQL_DELETE_FILM_GENRES =
            "DELETE FROM film_genres WHERE film_id = ?";

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpaId());
            return ps;
        }, keyHolder);

        Integer filmId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(filmId);

        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            for (Integer genreId : film.getGenreIds()) {
                jdbcTemplate.update(SQL_INSERT_FILM_GENRE, filmId, genreId);
            }
        }

        return enrichFilm(film);
    }

    @Override
    public Film update(Film film) {
        int rows = jdbcTemplate.update(SQL_UPDATE,
                film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpaId(), film.getId());

        if (rows == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        jdbcTemplate.update(SQL_DELETE_FILM_GENRES, film.getId());
        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            for (Integer genreId : film.getGenreIds()) {
                jdbcTemplate.update(SQL_INSERT_FILM_GENRE, film.getId(), genreId);
            }
        }

        return enrichFilm(film);
    }

    @Override
    public Film getById(Integer id) {
        List<Film> films = jdbcTemplate.query(SQL_FIND_BY_ID, filmMapper, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return enrichFilm(films.get(0));
    }

    @Override
    public List<Film> getAll() {
        List<Film> films = jdbcTemplate.query(SQL_FIND_ALL, filmMapper);
        for (Film film : films) {
            enrichFilm(film);
        }
        return films;
    }

    @Override
    public List<Film> getPopular(int count) {
        List<Film> films = jdbcTemplate.query(SQL_FIND_POPULAR, filmMapper, count);
        for (Film film : films) {
            enrichFilm(film);
        }
        return films;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        jdbcTemplate.update(SQL_ADD_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        jdbcTemplate.update(SQL_REMOVE_LIKE, filmId, userId);
    }

    public Set<Integer> getLikes(Integer filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Integer> likes = jdbcTemplate.queryForList(sql, Integer.class, filmId);
        return new HashSet<>(likes);
    }

    private Film enrichFilm(Film film) {
        // Загружаем MPA
        if (film.getMpaId() != null) {
            List<Mpa> mpaList = jdbcTemplate.query(SQL_FIND_MPA_BY_ID, mpaMapper, film.getMpaId());
            if (!mpaList.isEmpty()) {
                film.setMpa(mpaList.get(0));
            }
        }

        List<Genre> genres = jdbcTemplate.query(SQL_FIND_GENRES_BY_FILM_ID, genreMapper, film.getId());
        film.setGenres(new HashSet<>(genres));

        Set<Integer> genreIds = new HashSet<>();
        for (Genre g : genres) {
            genreIds.add(g.getId());
        }
        film.setGenreIds(genreIds);

        film.setLikes(getLikes(film.getId()));

        return film;
    }
}