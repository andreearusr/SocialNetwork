package com.map.socialnetwork.repository.eventRepository;

import com.map.socialnetwork.domain.Event;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.Pageable;

import java.util.List;

public interface EventPagingRepository {
    Page<Event> getAll(Pageable<Event> pageable, long id);

    Page<Event> getAttendingEvents(Pageable<Event> pageable, long id);
}
