package com.map.socialnetwork.repository;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.Tuple;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
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

    public void store(Friendship friendship) throws MissingEntityException, DuplicateEntityException {
        Tuple<Long, Long> ids = new Tuple<>(friendship.getId().first().getId(), friendship.getId().second().getId());
        Tuple<Long, Long> ids_swapped = new Tuple<>(friendship.getId().second().getId(), friendship.getId().first().getId());

        if (getFriendship(ids).isPresent() || getFriendship(ids_swapped).isPresent()) {
            throw new DuplicateEntityException("Could not send friend request to this user!");
        }

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

    public List<Friendship> getReceivedRequests(User user) {
        List<Friendship> requests = new ArrayList<>();

        String sql = """
                SELECT first_user, date from friendships WHERE second_user = (?)
                AND status='PENDING'
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                long userId = resultSet.getLong(1);
                Timestamp timestamp = resultSet.getTimestamp(2);

                User sender = userRepository.get(userId).orElse(User.deletedUser);
                requests.add(new Friendship(new Tuple<>(sender, user), timestamp, Friendship.Status.PENDING));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
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

    public List<User> getUnrelatedUsers(User user) {
        List<User> unrelatedUsers = new ArrayList<>();

        String sql = """
                SELECT u.id from users u
                WHERE (SELECT COUNT(*) FROM (
                        SELECT * from friendships as f
                        where (f.first_user=(?) AND f.second_user=u.id) OR (f.first_user=u.id AND f.second_user=(?))
                ) as unrelated) = 0 AND u.id != (?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.setLong(2, user.getId());
            ps.setLong(3, user.getId());
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                long userId = resultSet.getLong(1);

                userRepository.get(userId).ifPresent(unrelatedUsers::add);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return unrelatedUsers;
    }

    public List<Friendship> getAll(long id) {
        List<Friendship> friendships = new ArrayList<>();

        String sql = """
                SELECT * from friendships WHERE (first_user = (?) OR second_user = (?))
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                Long id1 = resultSet.getLong("first_user");
                Long id2 = resultSet.getLong("second_user");
                Friendship.Status status = Friendship.Status.valueOf(resultSet.getString("status").strip());
                Timestamp date = resultSet.getTimestamp("date");
                Friendship fr = new Friendship(new Tuple<User, User>(userRepository.get(id1).get(), userRepository.get(id2).get()), date, status);

                friendships.add(fr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendships;
    }

}
