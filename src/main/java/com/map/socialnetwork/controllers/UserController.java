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

    private long userId;
    ObservableList<User> model = FXCollections.observableArrayList();

    @FXML
    private TableColumn<User, String> FirstNameColumn;

    @FXML
    private TableColumn<User, String> LastNameColumn;

    @FXML
    private TableView<User> friendsTable;

    @FXML
    private Label loggedUser;


    public UserController() {
    }

    public void setService(Service service) {
        this.service = service;
        this.service.addObserver(this);
        this.userId = authentication.getUserId();
        initModel();
        loggedUser.setText(service.getUser(userId).get().toString());
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

        friendsTable.setItems(model);
    }

    private void initModel() {
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
        try {
            long firstUserid = authentication.getUserId();
            long secondUserid = friendsTable.getSelectionModel().getSelectedItem().getId();
            service.removeFriendship(firstUserid, secondUserid);
        } catch (NullPointerException nullPointerException) {
            MessageAlert.showErrorMessage(null, "Please select a user first!");
        }
    }

    @FXML
    public void handleFriendRequests() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("friendReq.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage dialogStage = new Stage();

        dialogStage.setTitle("Friendship requests");
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);

        FriendshipRequestsController friendshipRequestsController = fxmlLoader.getController();
        friendshipRequestsController.setAuthentication(authentication);
        friendshipRequestsController.setService(service);
        friendshipRequestsController.setStage(dialogStage);

        dialogStage.show();

    }

    @FXML
    public void handleLogout() throws AuthenticationException, IOException {
        authentication.logOut();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Meta");
        primaryStage.setScene(scene);

        LoginController loginController = fxmlLoader.getController();
        loginController.setAuthentication(authentication);
        loginController.setService(service);
        loginController.setStage(primaryStage);
    }

    @FXML
    public void respondToFriendRequest() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("respondToFriendRequest.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();

        RespondToFriendRequestController respondToFriendRequestController = fxmlLoader.getController();
        respondToFriendRequestController.setAuthentication(authentication);
        respondToFriendRequestController.setService(service);
    }

    @Override
    public void update(Observable o, Object arg) {
        initModel();
    }
}
