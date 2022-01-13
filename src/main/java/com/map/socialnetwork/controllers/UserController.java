package com.map.socialnetwork.controllers;

import com.map.socialnetwork.Main;
import com.map.socialnetwork.domain.Event;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.domain.UserPage;
import com.map.socialnetwork.exceptions.AuthenticationException;
import com.map.socialnetwork.exceptions.MissingEntityException;
import com.map.socialnetwork.exceptions.ValidatorException;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.PageableImpl;
import com.map.socialnetwork.service.Authenticator;
import com.map.socialnetwork.service.Service;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.shape.Box;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


public class UserController implements Observer {
    private Authenticator authenticator;
    private Service service;
    private Stage primaryStage;

    private User myUser;
    private UserPage userPage;
    private final ObservableList<User> model = FXCollections.observableArrayList();
    private Page<User> firstLoadedPage;
    private Page<User> secondLoadedPage;


    @FXML
    private TableColumn<User, String> FirstNameColumn;

    @FXML
    private TableColumn<User, String> LastNameColumn;

    @FXML
    private TableView<User> friendsTable;

    @FXML
    private Label loggedUser;

    public void setUserPage() {
        userPage.setFirstName(myUser.getFirstName());
        userPage.setLastName(myUser.getLastName());
        userPage.setFriends(service.getFriends(myUser));
        userPage.setFriendRequests(service.getAllFriendshipRequests(myUser.getId()));
        userPage.setReceivedMessages(service.getReceivedMessages(myUser.getId()));
    }


    public void setService(Service service) throws AuthenticationException, IOException {
        this.service = service;
        this.service.addObserver(this);

        Optional<User> user = service.getUser(authenticator.getUserId());

        if (user.isEmpty()) {
            handleLogout();
            return;
        }

        this.myUser = user.get();
        loggedUser.setText(myUser.toString());
        initModel();
    }

    public void setAuthentication(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    public void update(Observable o, Object arg) {
        initModel();
    }

    @FXML
    private void initialize() {
        FirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        LastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        friendsTable.setItems(model);

        Platform.runLater(() -> {
            ScrollBar tvScrollBar = (ScrollBar) friendsTable.lookup(".scroll-bar:vertical");
            tvScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((Double) newValue == 0.0) {
                    if (firstLoadedPage.getPageable().getPageNumber() > 1) {
                        secondLoadedPage = firstLoadedPage;
                        firstLoadedPage = service.getFriends(firstLoadedPage.previousPageable(), myUser);
                        setModel();
                    }
                } else if ((Double) newValue == 1.0) {
                    if (secondLoadedPage.getContent().size() == secondLoadedPage.getPageable().getPageSize()) {
                        Page<User> newUsers = service.getFriends(secondLoadedPage.nextPageable(), myUser);

                        if (!newUsers.getContent().isEmpty()) {
                            firstLoadedPage = secondLoadedPage;
                            secondLoadedPage = newUsers;
                            setModel();
                        }
                    }
                }
            });
        });

        Platform.runLater(() -> {
            for (Event e : service.getEventsUserSubscriber(myUser.getId())) {
                if (e.getDateReminder().toLocalDateTime().toLocalDate().isBefore(LocalDate.now()) ||
                        e.getDateReminder().toLocalDateTime().toLocalDate().isEqual(LocalDate.now())) {
                    if (Objects.equals(e.getDateReminder().toLocalDateTime().toLocalDate(), LocalDate.now())) {
                        Alert message = new Alert(Alert.AlertType.INFORMATION);
                        message.setTitle("Mesaj informare");
                        message.setContentText("Event " + e.getEventName() + " is coming in just 3 days!\nWe are waiting for you");
                        message.showAndWait();
                    }
                }
            }
        });
    }

    private void initModel() {
        firstLoadedPage = service.getFriends(new PageableImpl<>(1, 20), myUser);
        secondLoadedPage = service.getFriends(new PageableImpl<>(2, 20), myUser);

        setModel();
    }

    private void setModel() {
        List<User> loadedUsers = firstLoadedPage.getContent();
        loadedUsers.addAll(secondLoadedPage.getContent());
        model.clear();

        model.setAll(loadedUsers);
    }

    @FXML
    private void handleAddFriend() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("addFriend.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Add Friends");
        stage.show();

        AddFriendController addFriendController = fxmlLoader.getController();
        addFriendController.setUser(myUser);
        addFriendController.setService(service);
    }

    @FXML
    private void handleRemoveFriend() throws MissingEntityException {
        try {
            long firstUserid = authenticator.getUserId();
            long secondUserid = friendsTable.getSelectionModel().getSelectedItem().getId();
            service.removeFriendship(firstUserid, secondUserid);
        } catch (NullPointerException nullPointerException) {
            MessageAlert.showErrorMessage(null, "Please select a user first!");
        } catch (ValidatorException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    private void handleFriendRequests() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("friendReq.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage dialogStage = new Stage();

        dialogStage.setTitle("Friend requests");
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);

        FriendshipRequestsController friendshipRequestsController = fxmlLoader.getController();
        friendshipRequestsController.setUser(myUser);
        friendshipRequestsController.setService(service);

        dialogStage.show();
    }

    @FXML
    private void handleLogout() throws AuthenticationException, IOException {
        authenticator.logOut();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Go SOcial");
        primaryStage.setScene(scene);

        LoginController loginController = fxmlLoader.getController();
        loginController.setAuthentication(authenticator);
        loginController.setService(service);
        loginController.setStage(primaryStage);
    }

    @FXML
    private void handleRespondToFriendRequest() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("respondToFriendRequest.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("Respond to friend request");
        stage.show();

        RespondToFriendRequestController respondToFriendRequestController = fxmlLoader.getController();
        respondToFriendRequestController.setUser(myUser);
        respondToFriendRequestController.setService(service);
    }

    @FXML
    private void handleRetractFriendRequest() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("retractFriendRequest.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("Retract friend request");
        stage.show();

        RetractRequestController retractRequestController = fxmlLoader.getController();
        retractRequestController.setUser(myUser);
        retractRequestController.setService(service);
    }

    @FXML
    private void handleConversations() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("conversations.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();

        ConversationsController conversationsController = fxmlLoader.getController();
        conversationsController.setUser(myUser);
        conversationsController.setService(service);
    }

    @FXML
    private void handleSendMessage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("messageSender.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("Send a message");
        stage.show();

        MessageSenderController messageSenderController = fxmlLoader.getController();
        messageSenderController.setUser(myUser);
        messageSenderController.setService(service);
    }

    @FXML
    private void handleReports() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("reports.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("Reports");
        stage.show();

        ReportsController reportsController = fxmlLoader.getController();
        reportsController.setUser(myUser);
        reportsController.setService(service);
    }

    @FXML
    private void handleUserPage() throws IOException {
        try {
            User user = service.getUser(friendsTable.getSelectionModel().getSelectedItem().getId()).get();
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("userPage.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            primaryStage.setTitle("Go SOcial");
            primaryStage.setScene(scene);

            UserPageController userPageController = fxmlLoader.getController();
            userPageController.setService(service);
            userPageController.setAuthentication(authenticator);
            userPageController.setUserPage(user);
            userPageController.setUser(myUser);
            userPageController.setStage(primaryStage);

        } catch (NullPointerException nullPointerException) {
            MessageAlert.showErrorMessage(null, "Please select a user first!");
        }
    }

    @FXML
    private void handleAddEvent() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("addEvent.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("Add new event");
        stage.show();

        AddEventController addEventController = fxmlLoader.getController();
        addEventController.setService(service);
        addEventController.setUser(myUser);

    }
}
