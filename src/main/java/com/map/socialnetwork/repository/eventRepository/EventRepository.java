package com.map.socialnetwork.repository.eventRepository;

import com.map.socialnetwork.domain.Event;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.MissingEntityException;
import com.map.socialnetwork.repository.Repository;

import java.util.List;

public interface EventRepository extends Repository<String, Event>, EventPagingRepository {
    List<Event> getAll(long id) ;
    List<User> getParticipants(String eventName);
    List<User> getSubscribers(String eventName);
    void participateToEvent(Event event, long id) throws DuplicateEntityException;
    void unsubscribeAtEvent(Event event, long id) throws DuplicateEntityException, MissingEntityException;
}
