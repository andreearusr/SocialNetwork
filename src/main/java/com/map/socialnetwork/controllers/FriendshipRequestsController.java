package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.service.Authentication;
import com.map.socialnetwork.service.Service;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Timestamp;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class FriendshipRequestsController implements Observer {

    Service service;
    Authentication authentication;
    Stage primaryStage;

    private long userId;
    private final ObservableList<Friendship> model = FXCollections.observableArrayList();

    @FXML
    private TableColumn<Friendship, String> FromColumn;

    @FXML
    private TableColumn<Friendship, String> ToColumn;

    @FXML
    private TableColumn<Friendship, Friendship.Status> StatusColumn;

    @FXML
    private TableColumn<Friendship, Timestamp> DateColumn;

    @FXML
    private TableView<Friendship> tableRequests;

    public void setService(Service service) {
        this.service = service;
        this.service.addObserver(this);
        initModel();
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
        this.userId = authentication.getUserId();
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        FromColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().first().toString()));
        ToColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().second().toString()));
        StatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        DateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        tableRequests.setItems(model);
    }

    private void initModel() {
        List<Friendship> friendships = service.getAllFriendshipRequests(userId);
        model.setAll(friendships);
    }

    @Override
    public void update(Observable o, Object arg) {
        initModel();
    }

}
