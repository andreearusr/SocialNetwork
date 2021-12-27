package com.map.socialnetwork.domain.validator;


import com.map.socialnetwork.exceptions.ValidatorException;

/**
 * The interface Validator.
 *
 * @param <T> the type parameter
 */
public interface Validator<T> {
    /**
     * Validate.
     *
     * @param entity the entity
     * @throws ValidatorException the validator exception
     */
    void validate(T entity) throws ValidatorException;
}
