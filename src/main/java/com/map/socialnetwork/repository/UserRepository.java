package com.map.socialnetwork.repository;

import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.ValidatorException;

import java.util.List;

public interface UserRepository extends Repository<Long, User> {
    List<User> getAll();
    void update(Long id, User newUser) throws ValidatorException;
}
