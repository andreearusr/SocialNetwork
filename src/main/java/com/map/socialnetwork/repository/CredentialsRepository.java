package com.map.socialnetwork.repository;

import com.map.socialnetwork.domain.Credentials;
import com.map.socialnetwork.domain.validator.Validator;
import com.map.socialnetwork.exceptions.ValidatorException;

import java.sql.*;
import java.util.Optional;

public class CredentialsRepository extends AbstractRepository<Credentials> {
    public CredentialsRepository(String url, String username, String password, Validator<Credentials> validator) {
        super(url, username, password, validator);
    }

    public Optional<Long> getId(String user) {
        String sql = "SELECT * from credentials WHERE username = (?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Long id = resultSet.getLong("id");

                return Optional.of(id);
            }

            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public String getPassword(Long id) {
        String sql = "SELECT * from credentials WHERE id = (?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("password");
            }

            return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void store(Long userId, Credentials credentials) throws ValidatorException {
        validator.validate(credentials);
        String sql = "insert into credentials (id, username, password) values (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, credentials.getUsername());
            ps.setString(3, credentials.getPassword());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Long id, String newPassword) {
        String sql = "UPDATE credentials SET password = (?) WHERE id = (?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM credentials WHERE id = (?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
