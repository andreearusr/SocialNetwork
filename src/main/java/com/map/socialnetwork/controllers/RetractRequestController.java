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

public class RetractRequestController implements Observer {
    private Authentication authentication;
    private Service service;

    private long userId;

    private final ObservableList<Friendship> model = FXCollections.observableArrayList();

    @FXML
    private TableView<Friendship> requests;

    @FXML
    private TableColumn<Friendship, String> to;

    @FXML
    private TableColumn<Friendship, Timestamp> date;

    public void setService(Service service) {
        this.service = service;
        this.service.addObserver(this);
        initModel();
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
        this.userId = authentication.getUserId();
    }

    @Override
    public void update(Observable o, Object arg) {
        initModel();
    }

    @FXML
    private void initialize() {
        to.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getId().second().getFullName()));
        date.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        requests.setItems(model);
    }

    private void initModel() {
        List<Friendship> requests = service.getSentPendingRequests(service.getUser(userId).get());
        model.setAll(requests);
    }

    @FXML
    private void retractFriendRequest() {
        try {
            Long selectedUserId = requests.getSelectionModel().getSelectedItem().getId().second().getId();
            service.retractRequest(service.getUser(userId).orElseThrow(() -> new MissingEntityException("Invalid user!")),
                    service.getUser(selectedUserId).orElseThrow(() -> new MissingEntityException("Invalid user!")));
        } catch (MissingEntityException | InvalidRequestException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            MessageAlert.showErrorMessage(null, "Please select a request first!");
            e.printStackTrace();
        }
    }
}
