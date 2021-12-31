package com.map.socialnetwork.domain.validator;

import com.map.socialnetwork.domain.Credentials;
import com.map.socialnetwork.exceptions.ValidatorException;

public class CredentialsValidator implements Validator<Credentials> {
    @Override
    public void validate(Credentials credentials) throws ValidatorException {
        String errorMessage = "";

        if (credentials.getUsername().isEmpty()) {
            errorMessage += "Username is empty!\n";
        }

        if (credentials.getPassword().isEmpty()) {
            errorMessage += "Password is empty!\n";
        }

        if (!errorMessage.isEmpty()) {
            throw new ValidatorException(errorMessage);
        }
    }
}
