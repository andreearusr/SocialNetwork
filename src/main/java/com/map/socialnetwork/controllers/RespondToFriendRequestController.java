package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.exceptions.InvalidRequestException;
import com.map.socialnetwork.exceptions.MissingEntityException;
import com.map.socialnetwork.service.Authentication;
import com.map.socialnetwork.service.Service;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class RespondToFriendRequestController implements Observer {
    private Authentication authentication;
    private Service service;

    private long userId;

    ObservableList<Friendship> model = FXCollections.observableArrayList();

    @FXML
    private TableView<Friendship> requests;

    @FXML
    private TableColumn<Friendship, String> from;

    @FXML
    private TableColumn<Friendship, Timestamp> date;

    @FXML
    private Button accept;

    @FXML
    private Button reject;

    public void setService(Service service) {
        this.service = service;
        this.service.addObserver(this);
        this.userId = authentication.getUserId();
        initModel();
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    @FXML
    public void initialize() {
        from.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getId().first().getFullName()));
        date.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        requests.setItems(model);
    }

    private void initModel() {
        List<Friendship> friends = service.getReceivedRequests(service.getUser(userId).get());
        model.setAll(friends);
    }

    @Override
    public void update(Observable o, Object arg) {
        initModel();
    }

    @FXML
    private void acceptFriendRequest() {
        try {
            Long selectedUserId = requests.getSelectionModel().getSelectedItem().getId().first().getId();
            service.respondFriendshipRequest(selectedUserId, userId, Friendship.Status.ACCEPTED);
        } catch (MissingEntityException | InvalidRequestException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            MessageAlert.showErrorMessage(null, "Please select a request first!");
            e.printStackTrace();
        }
    }

    @FXML
    private void rejectFriendRequest() {
        try {
            Long selectedUserId = requests.getSelectionModel().getSelectedItem().getId().first().getId();
            service.respondFriendshipRequest(selectedUserId, userId, Friendship.Status.REJECTED);
        } catch (MissingEntityException | InvalidRequestException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            MessageAlert.showErrorMessage(null, "Please select a request first!");
            e.printStackTrace();
        }
    }
}
