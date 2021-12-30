package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.service.Service;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class FriendshipRequestsController implements Observer {
    private Service service;
    private User myUser;

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

    public void setUser(User user) {
        this.myUser = user;
    }

    @Override
    public void update(Observable o, Object arg) {
        initModel();
    }

    @FXML
    private void initialize() {
        FromColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().first().toString()));
        ToColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().second().toString()));
        StatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        DateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        tableRequests.setItems(model);
    }

    private void initModel() {
        List<Friendship> friendships = service.getAllFriendshipRequests(myUser.getId());
        model.setAll(friendships);
    }
}
