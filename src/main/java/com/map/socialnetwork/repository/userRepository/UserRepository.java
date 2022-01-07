package com.map.socialnetwork.repository.userRepository;

import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.ValidatorException;
import com.map.socialnetwork.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<Long, User>, UserPagingRepository {
    List<User> getAll();

    void update(Long id, User newUser) throws ValidatorException;

    Optional<User> getUser(String firstName, String lastName);
}
