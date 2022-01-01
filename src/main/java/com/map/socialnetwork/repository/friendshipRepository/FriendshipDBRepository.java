package com.map.socialnetwork.repository.friendshipRepository;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.Tuple;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.domain.validator.Validator;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.ValidatorException;
import com.map.socialnetwork.repository.AbstractRepository;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.PageImpl;
import com.map.socialnetwork.repository.paging.Pageable;
import com.map.socialnetwork.repository.userRepository.UserRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FriendshipDBRepository extends AbstractRepository<Friendship> implements FriendshipRepository {
    UserRepository userDBRepository;

    public FriendshipDBRepository(String url, String username, String password, UserRepository userDBRepository,
                                  Validator<Friendship> validator) {
        super(url, username, password, validator);
        this.userDBRepository = userDBRepository;
    }

    public void store(Friendship friendship) throws DuplicateEntityException, ValidatorException {
        validator.validate(friendship);

        if (get(friendship.getId()).isPresent() || get(
                new Tuple<>(friendship.getId().second(), friendship.getId().first())
        ).isPresent()) {
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

    public void delete(Friendship friendship) {
        String sql = "DELETE FROM friendships WHERE first_user=(?) AND second_user=(?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, friendship.getId().first().getId());
            ps.setLong(2, friendship.getId().second().getId());
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

    public void update(Friendship friendship) throws ValidatorException {
        validator.validate(friendship);
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
                    userDBRepository.get(firstUserId).ifPresent(friends::add);
                } else {
                    userDBRepository.get(secondUserId).ifPresent(friends::add);
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

                User sender = userDBRepository.get(userId).orElse(User.deletedUser);
                requests.add(new Friendship(new Tuple<>(sender, user), timestamp, Friendship.Status.PENDING));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public Optional<Friendship> get(Tuple<User, User> id) {
        Friendship friendship = null;

        String sql = """
                SELECT status, date from friendships WHERE first_user = (?)
                AND second_user=(?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id.first().getId());
            ps.setLong(2, id.second().getId());
            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                String status = resultSet.getString(1);
                Timestamp time = resultSet.getTimestamp(2);

                friendship = new Friendship(new Tuple<>(id.first(), id.second()),
                        time, Friendship.Status.valueOf(status));

                return Optional.of(friendship);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(friendship);
    }

    public List<Friendship> getSentPendingRequests(User user) {
        List<Friendship> requests = new ArrayList<>();

        String sql = """
                SELECT second_user, date from friendships WHERE first_user = (?)
                AND status='PENDING'
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                long userId = resultSet.getLong(1);
                Timestamp timestamp = resultSet.getTimestamp(2);

                User receiver = userDBRepository.get(userId).orElse(User.deletedUser);

                if (receiver == User.deletedUser) {
                    continue;
                }

                requests.add(new Friendship(new Tuple<>(user, receiver), timestamp, Friendship.Status.PENDING));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
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

                userDBRepository.get(userId).ifPresent(unrelatedUsers::add);
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
                Friendship fr = new Friendship(new Tuple<>(userDBRepository.get(id1).get(), userDBRepository.get(id2).get()), date, status);

                friendships.add(fr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendships;
    }

    @Override
    public Page<Friendship> getAll(Pageable<Friendship> pageable, long id) {
        List<Friendship> friendships = new ArrayList<>();

        String sql = """
                SELECT * from friendships WHERE (first_user = (?) OR second_user = (?))
                LIMIT (?) OFFSET (?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            ps.setLong(3, pageable.getPageSize());
            ps.setLong(4, (long) pageable.getPageSize() * (pageable.getPageNumber() - 1));
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                Long id1 = resultSet.getLong("first_user");
                Long id2 = resultSet.getLong("second_user");
                Friendship.Status status = Friendship.Status.valueOf(resultSet.getString("status").strip());
                Timestamp date = resultSet.getTimestamp("date");
                Friendship fr = new Friendship(new Tuple<>(userDBRepository.get(id1).get(), userDBRepository.get(id2).get()), date, status);

                friendships.add(fr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new PageImpl<>(pageable, friendships);
    }

    @Override
    public Page<User> getFriends(Pageable<User> pageable, User user) {
        List<User> friends = new ArrayList<>();

        String sql = """
                SELECT first_user, second_user from friendships WHERE (first_user = (?) OR second_user = (?))
                AND status='ACCEPTED'
                LIMIT (?) OFFSET (?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.setLong(2, user.getId());
            ps.setLong(3, pageable.getPageSize());
            ps.setLong(4, (long) pageable.getPageSize() * (pageable.getPageNumber() - 1));
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                long firstUserId = resultSet.getLong(1);
                long secondUserId = resultSet.getLong(2);

                if (firstUserId != user.getId()) {
                    userDBRepository.get(firstUserId).ifPresent(friends::add);
                } else {
                    userDBRepository.get(secondUserId).ifPresent(friends::add);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new PageImpl<>(pageable, friends);
    }

    @Override
    public Page<Friendship> getReceivedRequests(Pageable<Friendship> pageable, User user) {
        List<Friendship> requests = new ArrayList<>();

        String sql = """
                SELECT first_user, date from friendships WHERE second_user = (?)
                AND status='PENDING'
                LIMIT (?) OFFSET (?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.setLong(2, pageable.getPageSize());
            ps.setLong(3, (long) pageable.getPageSize() * (pageable.getPageNumber() - 1));
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                long userId = resultSet.getLong(1);
                Timestamp timestamp = resultSet.getTimestamp(2);

                User sender = userDBRepository.get(userId).orElse(User.deletedUser);
                requests.add(new Friendship(new Tuple<>(sender, user), timestamp, Friendship.Status.PENDING));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new PageImpl<>(pageable, requests);
    }

    @Override
    public Page<Friendship> getSentPendingRequests(Pageable<Friendship> pageable, User user) {
        List<Friendship> requests = new ArrayList<>();

        String sql = """
                SELECT second_user, date from friendships WHERE first_user = (?)
                AND status='PENDING'
                LIMIT (?) OFFSET (?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.setLong(2, pageable.getPageSize());
            ps.setLong(3, (long) pageable.getPageSize() * (pageable.getPageNumber() - 1));
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                long userId = resultSet.getLong(1);
                Timestamp timestamp = resultSet.getTimestamp(2);

                User receiver = userDBRepository.get(userId).orElse(User.deletedUser);

                if (receiver == User.deletedUser) {
                    continue;
                }

                requests.add(new Friendship(new Tuple<>(user, receiver), timestamp, Friendship.Status.PENDING));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new PageImpl<>(pageable, requests);
    }

    @Override
    public Page<User> getUnrelatedUsers(Pageable<User> pageable, User user) {
        List<User> unrelatedUsers = new ArrayList<>();

        String sql = """
                SELECT u.id from users u
                WHERE (SELECT COUNT(*) FROM (
                        SELECT * from friendships as f
                        where (f.first_user=(?) AND f.second_user=u.id) OR (f.first_user=u.id AND f.second_user=(?))
                ) as unrelated) = 0 AND u.id != (?)
                LIMIT (?) OFFSET (?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.setLong(2, user.getId());
            ps.setLong(3, user.getId());
            ps.setLong(4, pageable.getPageSize());
            ps.setLong(5, (long) pageable.getPageSize() * (pageable.getPageNumber() - 1));
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                long userId = resultSet.getLong(1);

                userDBRepository.get(userId).ifPresent(unrelatedUsers::add);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new PageImpl<>(pageable, unrelatedUsers);
    }

    public List<Friendship> getActivity(User user, Timestamp start, Timestamp end) {
        List<Friendship> friendships = new ArrayList<>();
        String sql = """
                SELECT first_user, second_user, date from friendships WHERE (first_user = (?) OR second_user = (?))
                AND status='ACCEPTED' AND (date BETWEEN (?) AND (?))
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.setLong(2, user.getId());
            ps.setTimestamp(3, start);
            ps.setTimestamp(4, end);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                long firstUserId = resultSet.getLong(1);
                long secondUserId = resultSet.getLong(2);
                Timestamp date = resultSet.getTimestamp(3);

                Optional<User> firstUser = userDBRepository.get(firstUserId);
                Optional<User> secondUser = userDBRepository.get(secondUserId);

                if (firstUser.isEmpty() || secondUser.isEmpty()) {
                    continue;
                }

                friendships.add(new Friendship(new Tuple<>(
                        firstUser.get(),
                        secondUser.get()
                ), date, Friendship.Status.ACCEPTED));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendships;
    }
}
