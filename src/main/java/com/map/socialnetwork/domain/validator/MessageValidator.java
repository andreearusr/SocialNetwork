package com.map.socialnetwork.domain.validator;

import com.map.socialnetwork.domain.Message;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.ValidatorException;

import java.util.Objects;

public class MessageValidator implements Validator<Message> {

    @Override
    public void validate(Message message) throws ValidatorException {
        StringBuilder errorMessage = new StringBuilder();

        if (message.getId() < 0) {
            errorMessage.append("Invalid id!\n");
        }

        if (message.getFrom().getId() < 0) {
            errorMessage.append("Invalid from id!\n");
        }

        if (message.getTo() == null) {
            errorMessage.append("Invalid recipient(s)!\n");
        } else {
            for (User user : message.getTo()) {
                if (user.getId() < 0) {
                    errorMessage.append("Invalid recipient ID: ").append(user.getId()).append("\n");
                }
            }
        }

        if (message.getMessage().isEmpty()) {
            errorMessage.append("Message body is empty!");
        }

        if (!Objects.equals(errorMessage.toString(), "")) {
            throw new ValidatorException(errorMessage.toString());
        }
    }
}
