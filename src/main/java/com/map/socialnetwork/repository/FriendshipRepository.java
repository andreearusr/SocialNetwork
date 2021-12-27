package com.map.socialnetwork.repository;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.Tuple;
import com.map.socialnetwork.domain.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FriendshipRepository extends AbstractRepository {
    public FriendshipRepository(String url, String username, String password) {
        super(url, username, password);
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
        //TODO WRITE FUNCTION
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

    //TODO WIRTE FUNCTIONS
    public List<User> getFriends(User user) {
       return new ArrayList<>();
    }

    public List<User> getRequested(User user) {
        return new ArrayList<>();
    }

    public Optional<Friendship> getFriendship(Tuple<Long, Long> id) {
        return Optional.empty();
    }
}
