package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaMapper;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaMapper mpaMapper;

    private static final String SQL_FIND_ALL = "SELECT * FROM mpa ORDER BY id";
    private static final String SQL_FIND_BY_ID = "SELECT * FROM mpa WHERE id = ?";

    @Override
    public List<Mpa> getAll() {
        return jdbcTemplate.query(SQL_FIND_ALL, mpaMapper);
    }

    @Override
    public Mpa getById(Integer id) {
        List<Mpa> mpaList = jdbcTemplate.query(SQL_FIND_BY_ID, mpaMapper, id);
        if (mpaList.isEmpty()) {
            throw new NotFoundException("Рейтинг с id=" + id + " не найден");
        }
        return mpaList.get(0);
    }
}