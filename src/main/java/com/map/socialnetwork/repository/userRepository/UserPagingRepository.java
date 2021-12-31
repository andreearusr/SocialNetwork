package com.map.socialnetwork.repository.userRepository;

import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.Pageable;

public interface UserPagingRepository {
    Page<User> getAll(Pageable<User> pageable);
}
