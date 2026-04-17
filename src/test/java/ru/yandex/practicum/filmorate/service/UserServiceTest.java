package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                new InMemoryUserStorage()
        );
    }

    @Test
    void shouldSetLoginAsNameIfNameEmpty() {
        User user = validUser();
        user.setName("");

        User created = userService.createUser(user);

        assertEquals(1, created.getId());
        assertEquals("login", created.getName());
    }

    @Test
    void shouldRejectInvalidEmail() {
        User user = validUser();
        user.setEmail("wrongmail");

        assertThrows(ValidationException.class, () -> userService.createUser(user));
    }

    @Test
    void shouldRejectEmptyLogin() {
        User user = validUser();
        user.setLogin("");

        assertThrows(ValidationException.class, () -> userService.createUser(user));
    }

    @Test
    void shouldRejectLoginWithSpaces() {
        User user = validUser();
        user.setLogin("my login");

        assertThrows(ValidationException.class, () -> userService.createUser(user));
    }

    @Test
    void shouldRejectFutureBirthday() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userService.createUser(user));
    }

    private User validUser() {
        User user = new User();
        user.setEmail("mail@example.com");
        user.setLogin("login");
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}