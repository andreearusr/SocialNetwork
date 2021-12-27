package com.map.socialnetwork.domain.validator;


import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.ValidatorException;

import java.util.Objects;

/**
 * The type User validator.
 */
public class UserValidator implements Validator<User> {

    @Override
    public void validate(User user) throws ValidatorException {
        String error = "";

        if (user.getId() < 0) {
            error += "Invalid User ID!\n";
        }

        if (Objects.equals(user.getFirstName(), "")) {
            error += "Invalid First Name!\n";
        }

        if (Objects.equals(user.getLastName(), "")) {
            error += "Invalid Last Name!\n";
        }

        if (!Objects.equals(error, "")) {
            throw new ValidatorException(error);
        }
    }
}
