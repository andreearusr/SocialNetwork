package com.map.socialnetwork.controllers;

import com.map.socialnetwork.Main;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.AuthenticationException;
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
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class UserController implements Observer {
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
        loggedUser.setText(service.getUser(authentication.getUserId()).get().toString());
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        FirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        LastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        IdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        friendsTable.setItems(model);
    }

    private void initModel() {
        long userId = authentication.getUserId();
        User user = service.getUser(userId).get();
        Iterable<User> friends = service.getFriends(user);
        List<User> friendsList = StreamSupport.stream(friends.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(friendsList);
    }

    @FXML
    public void handleAddFriend() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("addFriend.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        primaryStage.setTitle("Add Friend");
        primaryStage.setScene(scene);

        AddFriendController addFriendController = fxmlLoader.getController();
        addFriendController.setAuthentication(authentication);
        addFriendController.setService(service);
        addFriendController.setStage(primaryStage);
    }

    @FXML
    public void handleRemoveFriend() throws MissingEntityException {
        long id1 = authentication.getUserId();
        long id2 = friendsTable.getSelectionModel().getSelectedItem().getId();
        service.removeFriendship(id1, id2);

    }

    @FXML
    public void handleFriendRequests() {

    }


    @FXML
    public void handleLogout() throws AuthenticationException, IOException {
        authentication.logOut();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Meta");
        primaryStage.setScene(scene);

        LoginController loginController = fxmlLoader.getController();
        loginController.setService(service);
        loginController.setAuthentication(authentication);
        loginController.setStage(primaryStage);
    }

    @Override
    public void update(Observable o, Object arg) {
        initModel();
    }
}
