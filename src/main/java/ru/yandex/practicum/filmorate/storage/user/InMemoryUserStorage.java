package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new NotFoundException(
                    String.format("Пользователь с id=%s не найден", user.getId())
            );
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getById(Integer id) {
        User user = users.get(id);

        if (user == null) {
            throw new NotFoundException(
                    String.format("Пользователь с id=%d не найден", id)
            );
        }

        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }
}