package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService() {
        this.userStorage = new InMemoryUserStorage();
    }

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        return userStorage.update(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User getById(Integer id) {
        return userStorage.getById(id);
    }

    public void addFriend(Integer id, Integer friendId) {
        if (id.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        User user = getById(id);
        User friend = getById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);
    }

    public void removeFriend(Integer id, Integer friendId) {
        User user = getById(id);
        User friend = getById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
    }

    public List<User> getFriends(Integer id) {
        User user = getById(id);
        return user.getFriends().stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer id, Integer otherId) {
        User user = getById(id);
        User otherUser = getById(otherId);

        Set<Integer> friends = user.getFriends();
        Set<Integer> otherFriends = otherUser.getFriends();

        return friends.stream()
                .filter(otherFriends::contains)
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }
}