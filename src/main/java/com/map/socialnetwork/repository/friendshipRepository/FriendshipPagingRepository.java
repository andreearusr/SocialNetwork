package com.map.socialnetwork.repository.friendshipRepository;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.Pageable;

public interface FriendshipPagingRepository {
    Page<Friendship> getAll(Pageable<Friendship> pageable, long id);
    Page<User> getFriends(Pageable<User> pageable, User user);
    Page<Friendship> getReceivedRequests(Pageable<Friendship> pageable, User user);
    Page<Friendship> getSentPendingRequests(Pageable<Friendship> pageable, User user);
    Page<User> getUnrelatedUsers(Pageable<User> pageable, User user);
}
