package com.map.socialnetwork.domain;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class Event extends Entity<String>{

    private  String eventName;
    private final Timestamp date;
    private final Timestamp dateReminder;
    private final List<User> eventParticipants;
    private final long organizerId;

    public Event(String eventName, Timestamp date, List<User> eventParticipants, long organizerId) {
        super(eventName);
        this.date = date;
        this.dateReminder = Timestamp.valueOf(this.date.toLocalDateTime().minusDays(3));
        this.eventParticipants = eventParticipants;
        this.organizerId = organizerId;
    }

    public String getEventName(){return getId();}

    @Override
    public String toString() {
        return getId();
    }


}

