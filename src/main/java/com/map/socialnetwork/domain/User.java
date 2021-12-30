package com.map.socialnetwork.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class User extends Entity<Long> {
    public static final User deletedUser = new User("deleted", "user");

    private final String firstName;
    private final String lastName;

    /**
     * Instantiates a new User.
     *
     * @param id        long
     * @param firstName String
     * @param lastName  String
     */
    public User(Long id, String firstName, String lastName) {
        super(id);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(String firstName, String lastName) {
        super(0L);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    public String getFullName() {
        return firstName + ' ' + lastName;
    }
}