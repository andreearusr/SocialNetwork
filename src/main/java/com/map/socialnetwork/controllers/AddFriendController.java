package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.PageableImpl;
import com.map.socialnetwork.service.Service;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
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
    private Page<User> firstLoadedPage;
    private Page<User> secondLoadedPage;

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

        Platform.runLater(() -> {
            ScrollBar tvScrollBar = (ScrollBar) tableUsers.lookup(".scroll-bar:vertical");
            tvScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((Double) newValue == 0.0) {
                    if (firstLoadedPage.getPageable().getPageNumber() > 1) {
                        secondLoadedPage = firstLoadedPage;
                        firstLoadedPage = service.getUnrelatedUsers(firstLoadedPage.previousPageable(), myUser);
                        setModel();
                    }
                } else if ((Double) newValue == 1.0) {
                    if (secondLoadedPage.getContent().size() == secondLoadedPage.getPageable().getPageSize()) {
                        Page<User> newUsers = service.getUnrelatedUsers(secondLoadedPage.nextPageable(), myUser);

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
        firstLoadedPage = service.getUnrelatedUsers(new PageableImpl<>(1, 12), myUser);
        secondLoadedPage = service.getUnrelatedUsers(new PageableImpl<>(2, 12), myUser);
        setModel();
    }

    private void setModel() {
        List<User> unrelatedUsers = firstLoadedPage.getContent();
        unrelatedUsers.addAll(secondLoadedPage.getContent());
        model.setAll(unrelatedUsers);
    }

    @FXML
    private void handleAddFriend() {
        User user = tableUsers.getSelectionModel().getSelectedItem();

        try {
            service.sendFriendRequest(myUser.getId(), user.getId());
        } catch (NullPointerException nullPointerException) {
            MessageAlert.showErrorMessage(null, "Please select a user first!");
        } catch (Exception e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }


        initModel();
    }
}