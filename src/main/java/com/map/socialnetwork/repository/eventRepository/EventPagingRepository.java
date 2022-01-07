package com.map.socialnetwork.repository.eventRepository;

import com.map.socialnetwork.domain.Event;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.Pageable;

public interface EventPagingRepository {
    Page<Event> getAll(Pageable<Event> pageable, long id);
}
