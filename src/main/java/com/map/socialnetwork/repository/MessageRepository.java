package com.map.socialnetwork.repository;

import com.map.socialnetwork.domain.Message;
import com.map.socialnetwork.domain.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageRepository extends AbstractRepository{
    public MessageRepository(String url, String username, String password) {
        super(url, username, password);
    }

    public void store(Message message) {
        String saveMessage = "insert into messages (\"from\", reply, body, date ) values (?, ?, ?, ?)";
        String saveReceiver = "insert into users_messages (user_id, message_id) values (?, ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps1 = connection.prepareStatement(saveMessage, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement ps2 = connection.prepareStatement(saveReceiver)) {

            ps1.setLong(1, message.getFrom().getId());
            ps1.setLong(2, message.getReply() == null ? 0 : message.getReply().getId());
            ps1.setString(3, message.getMessage());
            ps1.setTimestamp(4, message.getTime());
            ps1.executeUpdate();

            try (ResultSet generatedKeys = ps1.getGeneratedKeys()) {
                if(generatedKeys.next()) {
                    ps2.setLong(2, generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creation of message failed. Could not obtain the ID!");
                }
            }

            for (User user : message.getTo()) {
                ps2.setLong(1, user.getId());
                ps2.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Message message) {
        String sql = "DELETE FROM users_messages WHERE message_id = (?);\n" +
                "DELETE FROM messages where id = (?);";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, message.getId());
            ps.setLong(2, message.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<Message> get(Long id) {
        //TODO WRITE FUNCTION
        return Optional.empty();
    }

    public List<Message> getConversation(User user1, User user2) {
        //TODO WRITE FUNCTION
        return new ArrayList<>();
    }
}
