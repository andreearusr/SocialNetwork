package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Event;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.ValidatorException;
import com.map.socialnetwork.service.Service;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AddEventController {

    private Service service;
    private User myUser;

    @FXML
    private TextField inputEventName;

    @FXML
    private DatePicker inputDate;

    public void setService(Service service) {
        this.service = service;
    }

    public void setUser(User user) {
        this.myUser = user;
    }


    @FXML
    private void initialize(){
        inputDate.setValue(LocalDate.from(LocalDateTime.now()));
        inputEventName.setText("");
    }

    @FXML
    public void handleAdd() {
        LocalDate date = inputDate.getValue();
        Event event = new Event(inputEventName.getText(), Timestamp.valueOf(date.atStartOfDay()), null, myUser.getId());

        try {
            service.addEvent(event);
            inputEventName.setText("");
        } catch (ValidatorException | DuplicateEntityException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }

    }
}