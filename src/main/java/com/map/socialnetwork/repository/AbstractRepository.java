package com.map.socialnetwork.repository;

import com.map.socialnetwork.domain.validator.Validator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AbstractRepository<E> {
    protected final String url;
    protected final String username;
    protected final String password;

    protected Validator<E> validator;
}
