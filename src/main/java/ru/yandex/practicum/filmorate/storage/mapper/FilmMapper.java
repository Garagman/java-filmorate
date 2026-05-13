package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Component
public class FilmMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getObject("release_date", java.time.LocalDate.class));
        film.setDuration(rs.getInt("duration"));
        film.setMpaId(rs.getObject("mpa_id", Integer.class));
        film.setGenres(new HashSet<>());
        film.setGenreIds(new HashSet<>());
        film.setLikes(new HashSet<>());
        return film;
    }
}