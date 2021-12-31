package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.PageableImpl;
import com.map.socialnetwork.service.Service;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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

public class FriendshipRequestsController implements Observer {
    private Service service;
    private User myUser;

    private final ObservableList<Friendship> model = FXCollections.observableArrayList();

    private Page<Friendship> firstLoadedPage;
    private Page<Friendship> secondLoadedPage;

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

        Platform.runLater(() -> {
            ScrollBar tvScrollBar = (ScrollBar) tableRequests.lookup(".scroll-bar:vertical");
            tvScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((Double)newValue == 0.0) {
                    if (firstLoadedPage.getPageable().getPageNumber() > 1) {
                        secondLoadedPage = firstLoadedPage;
                        firstLoadedPage = service.getAllFriendshipRequests(firstLoadedPage.previousPageable(), myUser.getId());
                        setModel();
                    }
                } else if ((Double)newValue == 1.0) {
                    if (secondLoadedPage.getContent().size() == secondLoadedPage.getPageable().getPageSize()) {
                        Page<Friendship> newUsers = service.getAllFriendshipRequests(secondLoadedPage.nextPageable(), myUser.getId());

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
        firstLoadedPage = service.getAllFriendshipRequests(new PageableImpl<>(1, 20), myUser.getId());
        secondLoadedPage = service.getAllFriendshipRequests(new PageableImpl<>(2, 20), myUser.getId());

        setModel();
    }

    private void setModel() {
        List<Friendship> friendships = firstLoadedPage.getContent();
        friendships.addAll(secondLoadedPage.getContent());
        model.setAll(friendships);
    }
}
