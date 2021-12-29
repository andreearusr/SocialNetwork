package com.map.socialnetwork.controllers;

import com.map.socialnetwork.Main;
import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.MissingEntityException;
import com.map.socialnetwork.service.Authentication;
import com.map.socialnetwork.service.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AddFriendController {

    Service service;
    Authentication authentication;
    Stage primaryStage;

    private long userId;
    ObservableList<User> model = FXCollections.observableArrayList();

    @FXML
    private TableColumn<User, String> FirstNameColumn;

    @FXML
    private TableColumn<User, String> LastNameColumn;

    @FXML
    private TableColumn<User, Long> IdColumn;

    @FXML
    private TableView<User> tableUsers;


    public void setService(Service service) {
        this.service = service;
        this.userId = authentication.getUserId();
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

        tableUsers.setItems(model);
    }

    private void initModel() {
        User user = service.getUser(userId).get();
        Iterable<User> friends = service.getRejected(user);
        List<User> friendsList = StreamSupport.stream(friends.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(friendsList);
    }

    @FXML
    public void handleAddFriend() throws MissingEntityException {
        User user = tableUsers.getSelectionModel().getSelectedItem();
        service.setFriendshipStatus(userId, user.getId(), Friendship.Status.ACCEPTED);
        initModel();
    }

    @FXML
    public void handleClose() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("userLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Social Network!");
        primaryStage.setScene(scene);

        UserController userController = fxmlLoader.getController();
        userController.setAuthentication(authentication);
        userController.setService(service);
        userController.setStage(primaryStage);
    }


}