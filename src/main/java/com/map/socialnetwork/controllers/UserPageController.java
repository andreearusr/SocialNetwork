package com.map.socialnetwork.controllers;

import com.map.socialnetwork.Main;
import com.map.socialnetwork.domain.Event;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.AuthenticationException;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.MissingEntityException;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class UserPageController implements Observer {

    private Authenticator authenticator;
    private Service service;
    private Stage primaryStage;

    private final ObservableList<User> model = FXCollections.observableArrayList();
    private final ObservableList<Event> modelEvents = FXCollections.observableArrayList();

    private User loggedUser;
    private User myUser;
    private Page<User> firstLoadedPage;
    private Page<User> secondLoadedPage;
    private Page<Event> firstLoadedEvent;
    private Page<Event> secondLoadedEvent;

    @FXML
    private TableColumn<User, String> FirstNameColumn;

    @FXML
    private TableColumn<User, String> LastNameColumn;

    @FXML
    private TableView<User> friendsTable;

    @FXML
    private Label userName;

    @FXML
    private Label eventDetails;

    @FXML
    private TableView<Event> eventsTable;

    @FXML
    private TableColumn<Event, String> EventNameColumn;



    public void setService(Service service) {
        this.service = service;
        this.service.addObserver(this);
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

    public void setUserPage(User myUser) {
        this.myUser = myUser;
        userName.setText(myUser.getFirstName() + " " + myUser.getLastName());

        initModel();
    }

    public void setUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    @FXML
    private void initialize() {
        FirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        LastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        EventNameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
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

        eventsTable.setItems(modelEvents);
        Platform.runLater(() -> {
            ScrollBar tvScrollBar = (ScrollBar) eventsTable.lookup(".scroll-bar:vertical");
            tvScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((Double) newValue == 0.0) {
                    if (firstLoadedEvent.getPageable().getPageNumber() > 1) {
                        secondLoadedEvent = firstLoadedEvent;
                        firstLoadedEvent = service.getAllEvents(firstLoadedEvent.previousPageable(), myUser.getId());
                        setModel();
                    }
                } else if ((Double) newValue == 1.0) {
                    if (secondLoadedEvent.getContent().size() == secondLoadedEvent.getPageable().getPageSize()) {
                        Page<Event> newEvents = service.getAllEvents(secondLoadedEvent.nextPageable(), myUser.getId());

                        if (!newEvents.getContent().isEmpty()) {
                            firstLoadedEvent = secondLoadedEvent;
                            secondLoadedEvent = newEvents;
                            setModel();
                        }
                    }
                }
            });
        });
    }

    private void initModel() {
        firstLoadedPage = service.getFriends(new PageableImpl<>(1, 11), myUser);
        secondLoadedPage = service.getFriends(new PageableImpl<>(2, 11), myUser);

        firstLoadedEvent = service.getAllEvents(new PageableImpl<>(1, 4), myUser.getId());
        secondLoadedEvent = service.getAllEvents(new PageableImpl<>(2, 4), myUser.getId());

        setModel();
    }

    private void setModel() {
        List<User> loadedUsers = firstLoadedPage.getContent();
        loadedUsers.addAll(secondLoadedPage.getContent());
        model.clear();
        model.setAll(loadedUsers);

        List<Event> loadedEvents = firstLoadedEvent.getContent();
        loadedEvents.addAll(secondLoadedEvent.getContent());
        modelEvents.clear();
        modelEvents.setAll(loadedEvents);
    }

    private void setDescription(){
        Event event = eventsTable.getSelectionModel().getSelectedItem();
        eventDetails.setText("Date: " + event.getDate() + "\nNumber of participants: " + event.getEventParticipants().size());
    }


    @FXML
    private void handleClose() throws IOException, AuthenticationException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("userLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Go SOcial");
        primaryStage.setScene(scene);

        UserController userController = fxmlLoader.getController();
        userController.setAuthentication(authenticator);
        userController.setService(service);
        userController.setStage(primaryStage);
    }

    @FXML
    private void handleSelectionEvent() {
        setDescription();
    }

    @FXML
    private void handleParticipate() {
        Event event = eventsTable.getSelectionModel().getSelectedItem();
        try {
            service.participateToEvent(event, loggedUser.getId());
        } catch (DuplicateEntityException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    private void handleUnsubscribe() {
        Event event = eventsTable.getSelectionModel().getSelectedItem();
        try {
            service.unsubscribeAtEvent(event, loggedUser.getId());
        } catch (MissingEntityException | DuplicateEntityException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }


}
