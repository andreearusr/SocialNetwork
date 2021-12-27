package com.map.socialnetwork.domain;

import java.io.Serial;
import java.io.Serializable;

/**
 * The type Tuple.
 *
 * @param <E1> the type parameter
 * @param <E2> the type parameter
 */
public record Tuple<E1, E2>(E1 first, E2 second) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
