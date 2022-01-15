package com.map.socialnetwork.repository.credentialsRepository;

import com.map.socialnetwork.domain.Credentials;
import com.map.socialnetwork.domain.validator.Validator;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.ValidatorException;
import com.map.socialnetwork.repository.AbstractRepository;

import java.sql.*;
import java.util.Optional;

public class CredentialsDBRepository extends AbstractRepository<Credentials> {
    public CredentialsDBRepository(String url, String username, String password, Validator<Credentials> validator) {
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

    public void store(Long userId, Credentials credentials) throws ValidatorException, DuplicateEntityException {
        validator.validate(credentials);
        if (checkIfExistsUsername(credentials.getUsername()))
            throw new DuplicateEntityException("This username already exists!");

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

    public Boolean checkIfExistsUsername(String userName) {

        String sql = """
                SELECT * FROM credentials 
                WHERE username=(?)
                 """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, userName);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                String userN = resultSet.getString("username");
                if(!userN.isEmpty())
                    return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
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
