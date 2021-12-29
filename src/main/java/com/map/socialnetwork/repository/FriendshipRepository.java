package com.map.socialnetwork.repository;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.Tuple;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.MissingEntityException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FriendshipRepository extends AbstractRepository {
    UserRepository userRepository;

    public FriendshipRepository(String url, String username, String password, UserRepository userRepository) {
        super(url, username, password);
        this.userRepository = userRepository;
    }

    public void store(Friendship friendship) {
        String sql = "insert into friendships (first_user, second_user, date, status) values (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, friendship.getId().first().getId());
            ps.setLong(2, friendship.getId().second().getId());
            ps.setTimestamp(3, friendship.getTimestamp());
            ps.setString(4, friendship.getStatus().toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteFriendshipsRelatedToUser(User user) {
        String sql = "DELETE FROM friendships WHERE first_user=(?) OR second_user=(?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.setLong(2, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Friendship friendship) {
        String sql = "UPDATE friendships SET status = (?) WHERE first_user = (?) AND second_user = (?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, friendship.getStatus().toString());
            ps.setLong(2, friendship.getId().first().getId());
            ps.setLong(3, friendship.getId().second().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> getFriends(User user) {
        List<User> friends = new ArrayList<>();

        String sql = """
                SELECT first_user, second_user from friendships WHERE (first_user = (?) OR second_user = (?))
                AND status='ACCEPTED'
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.setLong(2, user.getId());
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                long firstUserId = resultSet.getLong(1);
                long secondUserId = resultSet.getLong(2);

                if (firstUserId != user.getId()) {
                    userRepository.get(firstUserId).ifPresent(friends::add);
                } else {
                    userRepository.get(secondUserId).ifPresent(friends::add);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friends;
    }

    public List<User> getRequested(User user) {
        List<User> requested = new ArrayList<>();

        String sql = """
                SELECT first_user from friendships WHERE second_user = (?)
                AND status='PENDING'
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                long userId = resultSet.getLong(1);
                userRepository.get(userId).ifPresent(requested::add);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requested;
    }

    public Optional<Friendship> getFriendship(Tuple<Long, Long> id) throws MissingEntityException {
        Friendship friendship = null;

        User firstUser = userRepository.get(id.first()).orElseThrow(() -> new MissingEntityException("First id is invalid!"));
        User secondUSer = userRepository.get(id.second()).orElseThrow(() -> new MissingEntityException("Second id is invalid!"));

        String sql = """
                SELECT status, date from friendships WHERE first_user = (?)
                AND second_user=(?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id.first());
            ps.setLong(2, id.second());
            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                String status = resultSet.getString(1);
                Timestamp time = resultSet.getTimestamp(2);

                friendship = new Friendship(new Tuple<>(firstUser, secondUSer),
                        time, Friendship.Status.valueOf(status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(friendship);
    }

    public List<User> getRejected(User user) {
        List<User> rejected = new ArrayList<>();

        String sql = """
                SELECT first_user, second_user from friendships WHERE (first_user = (?) OR second_user = (?))
                AND status='REJECTED'
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.setLong(2, user.getId());
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                long firstUserId = resultSet.getLong(1);
                long secondUserId = resultSet.getLong(2);

                if (firstUserId != user.getId()) {
                    userRepository.get(firstUserId).ifPresent(rejected::add);
                } else {
                    userRepository.get(secondUserId).ifPresent(rejected::add);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rejected;
    }


}
