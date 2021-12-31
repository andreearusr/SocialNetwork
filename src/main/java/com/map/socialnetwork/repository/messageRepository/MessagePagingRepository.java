package com.map.socialnetwork.repository.messageRepository;

import com.map.socialnetwork.domain.Message;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.Pageable;

public interface MessagePagingRepository {
    Page<Message> getConversation(Pageable<Message> pageable, User firstUser, User secondUser);
}
