package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreMapper genreMapper;

    private static final String SQL_FIND_ALL = "SELECT * FROM genres ORDER BY id";
    private static final String SQL_FIND_BY_ID = "SELECT * FROM genres WHERE id = ?";

    @Override
    public List<Genre> getAll() {
        return jdbcTemplate.query(SQL_FIND_ALL, genreMapper);
    }

    @Override
    public Genre getById(Integer id) {
        List<Genre> genres = jdbcTemplate.query(SQL_FIND_BY_ID, genreMapper, id);
        if (genres.isEmpty()) {
            throw new NotFoundException("Жанр с id=" + id + " не найден");
        }
        return genres.get(0);
    }
}