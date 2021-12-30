package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.service.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class AddFriendController implements Observer {
    private Service service;
    private User myUser;

    private final ObservableList<User> model = FXCollections.observableArrayList();

    @FXML
    private TableColumn<User, String> FirstNameColumn;

    @FXML
    private TableColumn<User, String> LastNameColumn;

    @FXML
    private TableView<User> tableUsers;

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
        FirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        LastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        tableUsers.setItems(model);
    }

    private void initModel() {
        User user = myUser;
        List<User> friends = service.getUnrelatedUsers(user);

        model.setAll(friends);
    }

    @FXML
    private void handleAddFriend() {
        User user = tableUsers.getSelectionModel().getSelectedItem();

        try {
            service.sendFriendRequest(myUser.getId(), user.getId());
        } catch (Exception e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }

        initModel();
    }
}