package com.map.socialnetwork.repository.messageRepository;

import com.map.socialnetwork.domain.Message;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.repository.Repository;

import java.util.List;

public interface MessageRepository extends Repository<Long, Message>, MessagePagingRepository {
    List<Message> getConversation(User firstUser, User secondUser);
}
