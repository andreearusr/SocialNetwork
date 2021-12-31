package com.map.socialnetwork.repository;

import com.map.socialnetwork.domain.Entity;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.ValidatorException;

import java.util.Optional;

public interface Repository <ID, E extends Entity<ID> > {
    void store(E e) throws DuplicateEntityException, ValidatorException;
    Optional<E> get(ID id);
    void delete(E e);
}
