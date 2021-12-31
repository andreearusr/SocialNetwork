package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Entity;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.ValidatorException;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.PageableImpl;
import com.map.socialnetwork.service.Service;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

public class MessageSenderController implements Observer {
    private Service service;
    private User myUser;

    private final ObservableList<User> usersModel = FXCollections.observableArrayList();

    private Page<User> firstLoadedPage;
    private Page<User> secondLoadedPage;

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

    public void setUser(User user) {
        this.myUser = user;
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

        Platform.runLater(() -> {
            ScrollBar tvScrollBar = (ScrollBar) usersTable.lookup(".scroll-bar:vertical");
            tvScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((Double)newValue == 0.0) {
                    if (firstLoadedPage.getPageable().getPageNumber() > 1) {
                        secondLoadedPage = firstLoadedPage;
                        firstLoadedPage = service.getUsers(firstLoadedPage.previousPageable());
                        setModel();
                    }
                } else if ((Double)newValue == 1.0) {
                    if (secondLoadedPage.getContent().size() == secondLoadedPage.getPageable().getPageSize()) {
                        Page<User> newUsers = service.getUsers(secondLoadedPage.nextPageable());

                        if (!newUsers.getContent().isEmpty()) {
                            firstLoadedPage = secondLoadedPage;
                            secondLoadedPage = newUsers;
                            setModel();
                        }
                    }
                }
            });
        });
    }

    private void initModel() {
        firstLoadedPage = service.getUsers(new PageableImpl<>(1, 20));
        secondLoadedPage = service.getUsers(new PageableImpl<>(2, 20));

        setModel();
    }

    private void setModel() {
        List<User> users = firstLoadedPage.getContent();
        users.addAll(secondLoadedPage.getContent());
        users.removeIf(user -> user.getId().equals(myUser.getId()));
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

        try {
            service.sendSingleMessage(inputText.getText(), usersTable.getSelectionModel().getSelectedItems().stream()
                    .map(Entity::getId)
                    .collect(Collectors.toList()), myUser.getId());
        } catch (ValidatorException | DuplicateEntityException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
            e.printStackTrace();
        }
    }
}
