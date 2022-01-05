package com.map.socialnetwork.repository.messageRepository;

import com.map.socialnetwork.domain.Message;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.repository.Repository;

import java.sql.Timestamp;
import java.util.List;

public interface MessageRepository extends Repository<Long, Message>, MessagePagingRepository {
    List<Message> getConversation(User firstUser, User secondUser);
    List<Message> getConversationFromTimeSpan(User firstUser, User secondUser, Timestamp start, Timestamp end);
    List<Message> getActivity(User user, Timestamp start, Timestamp end);
    List<Message> getReceivedMessages(long id);
}
