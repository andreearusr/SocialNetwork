package com.map.socialnetwork.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * The type Entity.
 *
 * @param <E> the type parameter
 */
@Getter
@AllArgsConstructor
public class Entity<E> implements Serializable {
    private final E id;

    @Serial
    private static final long serialVersionUID = 1L;
}

