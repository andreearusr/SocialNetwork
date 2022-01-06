package com.map.socialnetwork.repository.eventRepository;

import com.map.socialnetwork.domain.*;

import com.map.socialnetwork.domain.validator.Validator;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.MissingEntityException;
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


public class EventDBRepository extends AbstractRepository<Event> implements EventRepository {

    UserRepository userDBRepository;

    public EventDBRepository(String url, String username, String password, UserRepository userDBRepository,
                             Validator<Event> validator) {
        super(url, username, password, validator);
        this.userDBRepository = userDBRepository;
    }

    public void store(Event event) throws DuplicateEntityException, ValidatorException {
        validator.validate(event);

        if (!get(event.getEventName()).isEmpty())
            throw new DuplicateEntityException("This event already exists");

        String sql = "insert into events (event_name, date, id_organizer) values (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, event.getEventName());
            ps.setTimestamp(2, event.getDate());
            ps.setLong(3, event.getOrganizerId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<Event> get(String eventName) {
        String sql = """
                SELECT * from events
                WHERE event_name=(?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, eventName);
            ResultSet resultSet = statement.executeQuery();

            Event event = null;

            if (resultSet.next()) {
                String name = resultSet.getString("event_name");
                Timestamp date = resultSet.getTimestamp("date");
                long idOrganizer = resultSet.getLong("id_organizer");

                List<User> eventParticipants = getParticipants(name);
                event = new Event(name, date, eventParticipants, idOrganizer);

            }

            return Optional.ofNullable(event);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public List<User> getParticipants(String eventName) {
        List<User> participants = new ArrayList<>();

        String sql = """
                SELECT user_id from users_events
                WHERE  event_name=(?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, eventName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                userDBRepository.get(resultSet.getLong("user_id")).ifPresent(participants::add);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return participants;
    }

    public List<User> getSubscribers(String eventName) {
        List<User> subscribers = new ArrayList<>();

        String sql = """
                SELECT user_id from users_events
                WHERE  event_name=(?) AND status='SUBSCRIBER'
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, eventName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                userDBRepository.get(resultSet.getLong("user_id")).ifPresent(subscribers::add);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return subscribers;
    }


    public void delete(Event event) {

    }

    public List<Event> getAll(long id) {
        List<Event> events = new ArrayList<>();

        String sql = """
                SELECT event_name from events
                WHERE id_organizer = (?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                get(resultSet.getString("event_name")).ifPresent(events::add);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return events;
    }

    @Override
    public Page<Event> getAll(Pageable<Event> pageable, long id) {
        List<Event> events = new ArrayList<>();

        String sql = """
                SELECT event_name from events 
                WHERE id_organizer=(?)
                LIMIT (?) OFFSET(?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setLong(2, pageable.getPageSize());
            statement.setLong(3, (long) pageable.getPageSize() * (pageable.getPageNumber() - 1));

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                get(resultSet.getString("event_name")).ifPresent(events::add);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return new PageImpl<>(pageable, events);
    }

    public Boolean checkIfParticipate(String eventName, long id) {
        String sql = """
                SELECT event_name FROM users_events 
                WHERE event_name=(?) AND user_id=(?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, eventName);
            statement.setLong(2, id);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                 if (get(resultSet.getString("event_name")).isPresent())
                     return true;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public Boolean checkIfUnsubscribe(String eventName, long id) {
        String sql = """
                SELECT event_name FROM users_events 
                WHERE event_name=(?) AND user_id=(?) AND status='UNSUBSCRIBE'
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, eventName);
            statement.setLong(2, id);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                if (get(resultSet.getString("event_name")).isPresent())
                    return true;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;

    }

    public void participateToEvent(Event event, long id) throws DuplicateEntityException {

        if (checkIfParticipate(event.getEventName(), id))
            throw new DuplicateEntityException("You are already participating in this event");

        String sql = "insert into users_events (event_name, user_id, status) values (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, event.getEventName());
            statement.setLong(2, id);
            statement.setString(3, "SUBSCRIBER");
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeAtEvent(Event event, long id) throws MissingEntityException, DuplicateEntityException {

        if (!checkIfParticipate(event.getEventName(), id))
            throw new MissingEntityException("You are not participating in this event");

        if (checkIfUnsubscribe(event.getEventName(), id))
            throw new DuplicateEntityException("You are already unsubscribed");


        String sql = """
                UPDATE users_events SET status = (?)
                WHERE user_id = (?) AND event_name = (?)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "UNSUBSCRIBE");
            ps.setLong(2, id);
            ps.setString(3, event.getEventName());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}

