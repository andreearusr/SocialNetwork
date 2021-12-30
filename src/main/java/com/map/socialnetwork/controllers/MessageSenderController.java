package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Entity;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.service.Authentication;
import com.map.socialnetwork.service.Service;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

public class MessageSenderController implements Observer {
    private Authentication authentication;
    private Service service;

    private long userId;

    private final ObservableList<User> usersModel = FXCollections.observableArrayList();

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> firstName;

    @FXML
    private TableColumn<User, String> lastName;

    @FXML
    private TextField inputText;

    public void setService(Service service) {
        this.service = service;
        this.service.addObserver(this);
        initModel();
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
        this.userId = authentication.getUserId();
    }

    @Override
    public void update(Observable o, Object arg) {
        initModel();
    }

    @FXML
    private void initialize() {
        firstName.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getFirstName()));
        lastName.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getLastName()));

        usersTable.setItems(usersModel);
        usersTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void initModel() {
        List<User> users = service.getUsers();
        users.removeIf(user -> user.getId().equals(userId));
        usersModel.setAll(users);
    }

    @FXML
    private void sendMessage() {
        if (usersTable.getSelectionModel().isEmpty()) {
            MessageAlert.showErrorMessage(null, "Please select at least a user to send message to!");
            return;
        }

        if (inputText.getText().isEmpty()) {
            MessageAlert.showErrorMessage(null, "Input text is empty!");
            return;
        }

        service.sendSingleMessage(inputText.getText(), usersTable.getSelectionModel().getSelectedItems().stream()
                .map(Entity::getId)
                .collect(Collectors.toList()), userId);
    }
}
