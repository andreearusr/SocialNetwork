package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.InvalidRequestException;
import com.map.socialnetwork.exceptions.MissingEntityException;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.PageableImpl;
import com.map.socialnetwork.service.Service;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
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

    private Page<Friendship> firstLoadedPage;
    private Page<Friendship> secondLoadedPage;

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

        Platform.runLater(() -> {
            ScrollBar tvScrollBar = (ScrollBar) requests.lookup(".scroll-bar:vertical");
            tvScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((Double)newValue == 0.0) {
                    if (firstLoadedPage.getPageable().getPageNumber() > 1) {
                        secondLoadedPage = firstLoadedPage;
                        firstLoadedPage = service.getSentPendingRequests(firstLoadedPage.previousPageable(), myUser);
                        setModel();
                    }
                } else if ((Double)newValue == 1.0) {
                    if (secondLoadedPage.getContent().size() == secondLoadedPage.getPageable().getPageSize()) {
                        Page<Friendship> newUsers = service.getSentPendingRequests(secondLoadedPage.nextPageable(), myUser);

                        if (!newUsers.getContent().isEmpty()) {
                            firstLoadedPage = secondLoadedPage;
                            secondLoadedPage = newUsers;
                            setModel();
                        }
                    }
                }
            });
        });
    }

    private void initModel() {
        firstLoadedPage = service.getSentPendingRequests(new PageableImpl<>(1, 7), myUser);
        secondLoadedPage = service.getSentPendingRequests(new PageableImpl<>(2, 7), myUser);

        setModel();
    }

    private void setModel() {
        List<Friendship> requests = firstLoadedPage.getContent();
        requests.addAll(secondLoadedPage.getContent());
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
