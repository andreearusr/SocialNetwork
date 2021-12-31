package com.map.socialnetwork.repository;

import com.map.socialnetwork.domain.Message;
import com.map.socialnetwork.domain.User;

import java.util.List;

public interface MessageRepository extends Repository<Long, Message> {
    List<Message> getConversation(User firstUser, User secondUser);
}
