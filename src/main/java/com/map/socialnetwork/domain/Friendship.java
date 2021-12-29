package com.map.socialnetwork.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * The type Friendship.
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@Getter
@Setter
public class Friendship extends Entity<Tuple<User, User>> {
    public enum Status {
        PENDING,
        REJECTED,
        ACCEPTED
    }

    private Timestamp timestamp;
    private Status status;

    /**
     * Instantiates a new Friendship.
     *
     * @param id long
     */
    public Friendship(Tuple<User, User> id, Timestamp timestamp, Status status) {
        super(id);
        this.timestamp = timestamp;
        this.status = status;
    }

    public Friendship(User firstUser, User secondUser) {
        super(new Tuple<>(firstUser, secondUser));
        this.timestamp = Timestamp.valueOf(LocalDateTime.now());
        this.status = Status.PENDING;
    }

    public void respond(User user, Status newStatus) {
        if (newStatus == Status.PENDING) {
            throw new RuntimeException("Invalid status!");
        }

        if (getId().second() != user) {
            throw new RuntimeException("User can not respond to friendship request!");
        }

        if (this.status != Status.PENDING) {
            throw new RuntimeException("Status is not Pending!");
        }

        this.status = newStatus;
        this.timestamp = Timestamp.valueOf(LocalDateTime.now());
    }
}

