package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.service.Authentication;
import com.map.socialnetwork.service.Service;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Timestamp;

public class AfterLoginController {
    private Authentication authentication;
    private Service service;
    private Stage primaryStage;

    @FXML
    private TextField messageForm;

    @FXML
    private Button sendMessageButton;

    @FXML
    private TableColumn<Friendship, String> fromColumn;

    @FXML
    private TableColumn<Friendship, String> toColumn;

    @FXML
    private TableColumn<Friendship.Status, String> statusColumn;

    @FXML
    private TableColumn<Friendship, Timestamp> dateColumn;

    @FXML
    private TableView<Friendship> friendRequestsTable;

    @FXML
    private TextArea messagesBox;

    @FXML
    private Label loggedUser;

    @FXML
    private Button logoutButton;

    public AfterLoginController() {
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void sendMessage() {

    }
}
