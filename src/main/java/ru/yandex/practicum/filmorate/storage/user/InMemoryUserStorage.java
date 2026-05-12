package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


    @Override
    public void addFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя");
        }
        User user = getById(userId);
        User friend = getById(friendId);

        // Проверяем обратную заявку
        FriendshipStatus reverseStatus = friend.getFriends().get(userId);
        if (reverseStatus == FriendshipStatus.UNCONFIRMED) {
            // Встречная заявка → подтверждаем взаимно
            user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);
        } else if (reverseStatus != FriendshipStatus.CONFIRMED) {
            // Новая односторонняя заявка
            user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
        }
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        User user = getById(userId);
        User friend = getById(friendId);
        // Одностороннее удаление
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    @Override
    public List<User> getFriends(Integer userId) {
        User user = getById(userId);
        return user.getFriends().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(entry -> getById(entry.getKey()))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        User user1 = getById(userId);
        User user2 = getById(otherId);

        var friends1 = user1.getFriends().entrySet().stream()
                .filter(e -> e.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        var friends2 = user2.getFriends().entrySet().stream()
                .filter(e -> e.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        friends1.retainAll(friends2);
        return friends1.stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }
}