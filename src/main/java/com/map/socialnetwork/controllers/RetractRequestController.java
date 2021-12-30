package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.InvalidRequestException;
import com.map.socialnetwork.exceptions.MissingEntityException;
import com.map.socialnetwork.service.Service;
import javafx.beans.property.ReadOnlyObjectWrapper;
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

public class RetractRequestController implements Observer {
    private Service service;
    private User myUser;
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

    public void setUser(User user) {
        this.myUser = user;
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
        List<Friendship> requests = service.getSentPendingRequests(myUser);
        model.setAll(requests);
    }

    @FXML
    private void retractFriendRequest() {
        try {
            Long selectedUserId = requests.getSelectionModel().getSelectedItem().getId().second().getId();
            service.retractRequest(myUser,
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
