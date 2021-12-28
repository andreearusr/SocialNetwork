package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Credentials;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.AuthenticationException;
import com.map.socialnetwork.service.Authentication;
import com.map.socialnetwork.service.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class UserController {
    private Authentication authentication;
    private Service service;
    private Stage primaryStage;
    ObservableList<User> model = FXCollections.observableArrayList();

    @FXML
    private TableColumn<User, String> FirstNameColumn;

    @FXML
    private TableColumn<User, String> LastNameColumn;

    @FXML
    private TableColumn<User, Long> IdColumn;

    @FXML
    private TableView<User> friendsTable;

    @FXML
    private Label loggedUser;


    public UserController() {
    }

    public void setService(Service service) {
        this.service = service;
        initModel();
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        FirstNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        LastNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));
        IdColumn.setCellValueFactory(new PropertyValueFactory<User, Long>("id"));

        friendsTable.setItems(model);
    }

    private void initModel() {
        long userId = authentication.getUserId();
        User user = service.getUser(userId).get();
        Iterable<User> friends = service.getFriends(user);
        List<User> friendsList = StreamSupport.stream(friends.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(friendsList);

        loggedUser.setText(service.getUser(userId).get().toString());

    }


    @FXML
    public void handleAddFriend() {

    }

    @FXML
    public void handleRemoveFriend() {

    }

    @FXML
    public void handleFriendRequests() {

    }


    @FXML
    public void handleLogout() throws AuthenticationException {
        primaryStage.close();
        authentication.logOut();
    }

}
