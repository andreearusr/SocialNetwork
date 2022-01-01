package com.map.socialnetwork.repository.messageRepository;

import com.map.socialnetwork.domain.Message;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.domain.validator.Validator;
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

public class MessageDBRepository extends AbstractRepository<Message> implements MessageRepository {
    UserRepository userDBRepository;

    public MessageDBRepository(String url, String username, String password, UserRepository userDBRepository,
                               Validator<Message> validator) {
        super(url, username, password, validator);
        this.userDBRepository = userDBRepository;
    }

    public void store(Message message) throws ValidatorException {
        validator.validate(message);
        String saveMessage = "insert into messages (\"from\", reply, body, date ) values (?, ?, ?, ?)";
        String saveReceiver = "insert into users_messages (user_id, message_id) values (?, ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps1 = connection.prepareStatement(saveMessage, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement ps2 = connection.prepareStatement(saveReceiver)) {

            ps1.setLong(1, message.getFrom().getId());
            ps1.setLong(2, message.getReply() == null ? 0 : message.getReply().getId());
            ps1.setString(3, message.getMessage());
            ps1.setTimestamp(4, message.getTimestamp());
            ps1.executeUpdate();

            try (ResultSet generatedKeys = ps1.getGeneratedKeys()) {
                if (generatedKeys.next()) {
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
        String sql = """
                SELECT * from messages
                WHERE  id=(?)""";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            Message message = null;

            if (resultSet.next()) {
                long messageId = resultSet.getLong("id");
                String body = resultSet.getString("body");
                Timestamp timestamp = resultSet.getTimestamp("date");
                long from = resultSet.getLong("from");
                long replyId = resultSet.getLong("reply");

                Message reply = null;

                if (replyId != 0) {
                    Optional<Message> replyOp = get(replyId);

                    reply = replyOp.orElse(null);
                }

                List<User> to = getReceivers(id);

                message = new Message(messageId, body, to, userDBRepository.get(from).orElse(null),
                        Timestamp.valueOf(timestamp.toLocalDateTime()), reply);
            }

            return Optional.ofNullable(message);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private List<User> getReceivers(long messageId) {
        List<User> receivers = new ArrayList<>();

        String sql = """
                SELECT user_id from users_messages
                WHERE  message_id=(?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, messageId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                userDBRepository.get(resultSet.getLong("user_id")).ifPresent(receivers::add);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return receivers;
    }

    public List<Message> getConversation(User firstUser, User secondUser) {
        List<Message> messages = new ArrayList<>();

        String sql = """
                SELECT m.id from messages m\040
                inner join users_messages um on m.id = um.message_id
                WHERE (m."from"=(?) AND um.user_id=(?)) OR (m."from"=(?) AND um.user_id=(?))
                ORDER BY m.date
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, firstUser.getId());
            statement.setLong(2, secondUser.getId());
            statement.setLong(3, secondUser.getId());
            statement.setLong(4, firstUser.getId());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                get(resultSet.getLong("id")).ifPresent(messages::add);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return messages;
    }

    @Override
    public Page<Message> getConversation(Pageable<Message> pageable, User firstUser, User secondUser) {
        List<Message> messages = new ArrayList<>();

        String sql = """
                SELECT m.id from messages m\040
                inner join users_messages um on m.id = um.message_id
                WHERE (m."from"=(?) AND um.user_id=(?)) OR (m."from"=(?) AND um.user_id=(?))
                ORDER BY m.date
                LIMIT (?) OFFSET(?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, firstUser.getId());
            statement.setLong(2, secondUser.getId());
            statement.setLong(3, secondUser.getId());
            statement.setLong(4, firstUser.getId());
            statement.setLong(5, pageable.getPageSize());
            statement.setLong(6, (long) pageable.getPageSize() * (pageable.getPageNumber() - 1));
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                get(resultSet.getLong("id")).ifPresent(messages::add);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return new PageImpl<>(pageable, messages);
    }

    public List<Message> getConversationFromTimeSpan(User firstUser, User secondUser, Timestamp start, Timestamp end) {
        List<Message> messages = new ArrayList<>();

        String sql = """
                SELECT m.id from messages m\040
                inner join users_messages um on m.id = um.message_id
                WHERE (m."from"=(?) AND um.user_id=(?))
                AND (m.date between (?) AND (?))
                ORDER BY m.date
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, secondUser.getId());
            statement.setLong(2, firstUser.getId());
            statement.setTimestamp(3, start);
            statement.setTimestamp(4, end);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                get(resultSet.getLong("id")).ifPresent(messages::add);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return messages;
    }

    public List<Message> getActivity(User user, Timestamp start, Timestamp end) {
        List<Message> messages = new ArrayList<>();

        String sql = """
                SELECT m.id from messages m\040
                inner join users_messages um on m.id = um.message_id
                WHERE um.user_id=(?)
                AND (m.date between (?) AND (?))
                ORDER BY m.date
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, user.getId());
            statement.setTimestamp(2, start);
            statement.setTimestamp(3, end);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                get(resultSet.getLong("id")).ifPresent(messages::add);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return messages;
    }
}
