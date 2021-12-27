package com.map.socialnetwork.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class Message extends Entity<Long> {

    public static final Message deletedMessage = new Message("deleted", new ArrayList<>(), User.deletedUser, null);

    private final String message;
    private final List<User> to;
    private final User from;
    private final Timestamp time;
    private final Message reply;

    public Message(Long id, String message, List<User> to, User from, Message reply) {
        super(id);
        this.message = message;
        this.to = to;
        this.from = from;
        this.time = Timestamp.valueOf(LocalDateTime.now());
        this.reply = reply;
    }

    public Message(Long id, String message, List<User> to, User from, Timestamp time, Message reply) {
        super(id);
        this.message = message;
        this.to = to;
        this.from = from;
        this.time = time;
        this.reply = reply;
    }

    public Message(String message, List<User> to, User from, Message reply) {
        super(0L);
        this.message = message;
        this.to = to;
        this.from = from;
        this.time = Timestamp.valueOf(LocalDateTime.now());
        this.reply = reply;
    }

    @Override
    public String toString() {
        if (from.getId() == 3)
            System.out.println(to);
        return String.format("MessageId: %d\n", getId()) +
                String.format("From: %s\n", from) +
                String.format("To: %s\n", to) +
                String.format("Message: %s\n", message) +
                ((reply == null) ? "" : String.format("ReplyId: %d\n", reply.getId()));
    }
}
