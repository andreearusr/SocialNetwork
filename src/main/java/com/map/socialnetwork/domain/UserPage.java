package com.map.socialnetwork.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class UserPage {

    private String firstName;
    private String lastName;
    private List<User> friends;
    private List<Friendship> friendRequests;
    private List<Message> receivedMessages;


    public UserPage(String firstName, String lastName, List<User> friends, List<Friendship> friendRequests, List<Message> receivedMessages) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.friends = friends;
        this.friendRequests = friendRequests;
        this.receivedMessages = receivedMessages;
    }

}
