package com.map.socialnetwork.domain.validator;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.exceptions.ValidatorException;

import java.util.Objects;

/**
 * The type Friendship validator.
 */
public class FriendshipValidator implements Validator<Friendship> {

    @Override
    public void validate(Friendship friendship) throws ValidatorException {
        String errorMessage = "";

        if (friendship.getId().first().getId() < 0) {
            errorMessage += "First ID is invalid!\n";
        }

        if (friendship.getId().second().getId() < 0) {
            errorMessage += "Second ID is invalid!\n";
        }

        if (Objects.equals(friendship.getId().second(), friendship.getId().first())) {
            errorMessage += "IDs must be different!\n";
        }

        if (!Objects.equals(errorMessage, "")) {
            throw new ValidatorException(errorMessage);
        }
    }
}

