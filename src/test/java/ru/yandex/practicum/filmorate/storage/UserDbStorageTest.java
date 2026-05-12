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

    @Test
    void shouldManageFriendships_OneSided() {
        User u1 = userStorage.create(createTestUser());
        User u2 = userStorage.create(createTestUser2());

        userStorage.addFriend(u1.getId(), u2.getId());

        Map<Integer, FriendshipStatus> u1Friends = userStorage.getById(u1.getId()).getFriends();
        Map<Integer, FriendshipStatus> u2Friends = userStorage.getById(u2.getId()).getFriends();

        assertThat(u1Friends.get(u2.getId())).isEqualTo(FriendshipStatus.UNCONFIRMED);
        assertThat(u2Friends).doesNotContainKey(u1.getId());

        userStorage.addFriend(u2.getId(), u1.getId());

        u1Friends = userStorage.getById(u1.getId()).getFriends();
        u2Friends = userStorage.getById(u2.getId()).getFriends();

        assertThat(u1Friends.get(u2.getId())).isEqualTo(FriendshipStatus.CONFIRMED);
        assertThat(u2Friends.get(u1.getId())).isEqualTo(FriendshipStatus.CONFIRMED);

        userStorage.removeFriend(u1.getId(), u2.getId());

        u1Friends = userStorage.getById(u1.getId()).getFriends();
        u2Friends = userStorage.getById(u2.getId()).getFriends();

        assertThat(u1Friends).doesNotContainKey(u2.getId());
        assertThat(u2Friends).doesNotContainKey(u1.getId());
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