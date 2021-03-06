package com.map.socialnetwork.repository.userRepository;

import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.domain.validator.Validator;
import com.map.socialnetwork.exceptions.ValidatorException;
import com.map.socialnetwork.repository.AbstractRepository;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.PageImpl;
import com.map.socialnetwork.repository.paging.Pageable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDBRepository extends AbstractRepository<User> implements UserRepository {

    public UserDBRepository(String url, String username, String password, Validator<User> validator) {
        super(url, username, password, validator);
    }

    public void store(User user) throws ValidatorException {
        validator.validate(user);

        String sql = """
                insert into users(first_name, last_name ) values (?, ?)""";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(User user) {
        String sql = "DELETE FROM users WHERE id = (?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<User> get(Long id) {
        String sql = "SELECT * from users WHERE id = (?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                return Optional.of(new User(id, firstName, lastName));
            }

            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public long getLastUserAdded() {
        String sql = """
                SELECT id FROM users AS u
                WHERE u.id=(SELECT max(u.id) FROM users AS u)
                 """;

        long id = 0L;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong("id");
            }

            return id;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }


    public List<User> getAll() {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * from users";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                User user = new User(id, firstName, lastName);
                users.add(user);
            }

            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public void update(Long id, User newUser) throws ValidatorException {
        validator.validate(newUser);
        String sql = "UPDATE users SET first_name = (?), last_name = (?) WHERE id = (?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newUser.getFirstName());
            ps.setString(2, newUser.getLastName());
            ps.setLong(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Page<User> getAll(Pageable<User> pageable) {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT * from users
                LIMIT (?) OFFSET (?)""";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, pageable.getPageSize());
            statement.setLong(2, (long) pageable.getPageSize() * (pageable.getPageNumber() - 1));
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                User user = new User(id, firstName, lastName);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new PageImpl<>(pageable, users);
    }
}
