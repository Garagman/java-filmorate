package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserMapper;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    private static final String SQL_INSERT =
            "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM users WHERE id = ?";

    private static final String SQL_FIND_ALL =
            "SELECT * FROM users";

    private static final String SQL_FIND_FRIENDS =
            "SELECT u.* FROM users u " +
                    "JOIN friendships f ON u.id = f.friend_id " +
                    "WHERE f.user_id = ?";

    private static final String SQL_FIND_COMMON_FRIENDS =
            "SELECT u.* FROM users u " +
                    "WHERE u.id IN (" +
                    "    SELECT friend_id FROM friendships WHERE user_id = ?" +
                    ") " +
                    "AND u.id IN (" +
                    "    SELECT friend_id FROM friendships WHERE user_id = ?" +
                    ") " +
                    "AND u.id != ? AND u.id != ?";

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }

    @Override
    public User update(User user) {
        int rows = jdbcTemplate.update(SQL_UPDATE,
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (rows == 0) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        return user;
    }

    @Override
    public User getById(Integer id) {
        List<User> users = jdbcTemplate.query(SQL_FIND_BY_ID, userMapper, id);
        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        User user = users.get(0);
        user.setFriends(loadFriendships(user.getId()));
        return user;
    }

    @Override
    public List<User> getAll() {
        List<User> users = jdbcTemplate.query(SQL_FIND_ALL, userMapper);
        for (User user : users) {
            user.setFriends(loadFriendships(user.getId()));
        }
        return users;
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя");
        }

        getById(userId);
        getById(friendId);

        String currentStatus = jdbcTemplate.query(
                "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?",
                rs -> rs.next() ? rs.getString("status") : null, userId, friendId);

        String reverseStatus = jdbcTemplate.query(
                "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?",
                rs -> rs.next() ? rs.getString("status") : null, friendId, userId);

        if ("CONFIRMED".equals(currentStatus)) {
            return;
        }

        if ("UNCONFIRMED".equals(reverseStatus)) {
            jdbcTemplate.update(
                    "UPDATE friendships SET status = 'CONFIRMED' WHERE user_id = ? AND friend_id = ?",
                    friendId, userId);
            jdbcTemplate.update(
                    "MERGE INTO friendships (user_id, friend_id, status) KEY (user_id, friend_id) VALUES (?, ?, 'CONFIRMED')",
                    userId, friendId);
        } else if (currentStatus == null) {
            jdbcTemplate.update(
                    "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'UNCONFIRMED')",
                    userId, friendId);
        }
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        getById(userId);
        getById(friendId);
        jdbcTemplate.update(
                "DELETE FROM friendships WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)",
                userId, friendId, friendId, userId);
    }

    @Override
    public List<User> getFriends(Integer userId) {
        getById(userId);
        return jdbcTemplate.query(SQL_FIND_FRIENDS, userMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        getById(userId);
        getById(otherId);
        return jdbcTemplate.query(SQL_FIND_COMMON_FRIENDS, userMapper, userId, otherId, userId, otherId);
    }

    private Map<Integer, FriendshipStatus> loadFriendships(Integer userId) {
        String sql = "SELECT friend_id, status FROM friendships WHERE user_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId);
        Map<Integer, FriendshipStatus> friends = new HashMap<>();
        for (Map<String, Object> row : rows) {
            friends.put(
                    (Integer) row.get("friend_id"),
                    FriendshipStatus.valueOf((String) row.get("status"))
            );
        }
        return friends;
    }
}