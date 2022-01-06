package com.map.socialnetwork.domain.validator;

import com.map.socialnetwork.domain.Event;
import com.map.socialnetwork.exceptions.ValidatorException;

import java.util.Objects;

public class EventsValidator implements Validator<Event> {
    @Override
    public void validate(Event entity) throws ValidatorException {
        String errorMessage = "";

        if (entity.getEventName().isEmpty()) {
            errorMessage += "Event name is null!\n";
        }

        if (entity.getOrganizerId() < 0) {
            errorMessage += "Organizer ID is invalid!\n";
        }

        if (!Objects.equals(errorMessage, "")) {
            throw new ValidatorException(errorMessage);
        }

    }
}
