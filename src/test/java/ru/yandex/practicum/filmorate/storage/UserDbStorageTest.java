package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void shouldCreateUser() {
        User user = createTestUser();
        User created = userStorage.create(user);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void shouldFindUserById() {
        User created = userStorage.create(createTestUser());
        User found = userStorage.getById(created.getId());
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getLogin()).isEqualTo("testlogin");
    }

    @Test
    void shouldThrowNotFoundWhenUserNotExist() {
        assertThatThrownBy(() -> userStorage.getById(9999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldUpdateUser() {
        User user = userStorage.create(createTestUser());
        user.setName("Updated Name");
        userStorage.update(user);
        User updated = userStorage.getById(user.getId());
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldGetAllUsers() {
        userStorage.create(createTestUser());
        userStorage.create(createTestUser2());
        List<User> users = userStorage.getAll();
        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }


    private User createTestUser() {
        User u = new User();
        u.setEmail("test@test.com");
        u.setLogin("testlogin");
        u.setName("User1");
        u.setBirthday(LocalDate.of(1990, 1, 1));
        return u;
    }

    private User createTestUser2() {
        User u = new User();
        u.setEmail("test2@test.com");
        u.setLogin("testlogin2");
        u.setName("User2");
        u.setBirthday(LocalDate.of(1991, 1, 1));
        return u;
    }
}