package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaDbStorage.class, MpaMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void shouldGetAllMpa() {
        List<Mpa> mpaList = mpaStorage.getAll();
        assertThat(mpaList).hasSize(5);
        assertThat(mpaList.stream().map(Mpa::getName))
                .containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void shouldGetMpaById() {
        Mpa mpa = mpaStorage.getById(3);
        assertThat(mpa.getName()).isEqualTo("PG-13");
    }

    @Test
    void shouldThrowNotFoundWhenMpaNotExist() {
        assertThatThrownBy(() -> mpaStorage.getById(99))
                .isInstanceOf(NotFoundException.class);
    }
}