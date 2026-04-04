package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserService {
    private final Map<Integer, User> users = new LinkedHashMap<>();
    private int nextId = 1;

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public User createUser(User user) {
        validateUser(user);
        normalizeName(user);
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    public User updateUser(User user) {
        validateUser(user);

        if (user.getId() == null || user.getId() <= 0) {
            log.warn("Ошибка валидации пользователя: некорректный id");
            throw new ValidationException("Id пользователя должен быть положительным числом");
        }

        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        normalizeName(user);
        users.put(user.getId(), user);
        log.info("Обновлен пользователь: {}", user);
        return user;
    }

    private void validateUser(User user) {
        if (user == null) {
            log.warn("Ошибка валидации пользователя: тело запроса пустое");
            throw new ValidationException("Тело запроса не должно быть пустым");
        }

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации пользователя: некорректный email");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации пользователя: некорректный login");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации пользователя: некорректная дата рождения");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void normalizeName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}