package com.map.socialnetwork.controllers;

import com.map.socialnetwork.Main;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.AuthenticationException;
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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    private User loggedUser;
    private User myUser;
    private Page<User> firstLoadedPage;
    private Page<User> secondLoadedPage;

    @FXML
    private TableColumn<User, String> FirstNameColumn;

    @FXML
    private TableColumn<User, String> LastNameColumn;

    @FXML
    private TableView<User> friendsTable;

    @FXML
    private Label userName;

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

    public void setUser(User loggedUser){
        this.loggedUser = loggedUser;
    }

    @FXML
    private void initialize() {
        FirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        LastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
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
    }

    private void initModel() {
        firstLoadedPage = service.getFriends(new PageableImpl<>(1, 20), myUser);
        secondLoadedPage = service.getFriends(new PageableImpl<>(2, 20), myUser);

        setModel();
    }

    private void setModel() {
        List<User> loadedUsers = firstLoadedPage.getContent();
        loadedUsers.addAll(secondLoadedPage.getContent());
        model.clear();

        model.setAll(loadedUsers);
    }


    @FXML
    private void handleClose() throws IOException, AuthenticationException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("userLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Meta");
        primaryStage.setScene(scene);

        UserController userController = fxmlLoader.getController();
        userController.setAuthentication(authenticator);
        userController.setService(service);
        userController.setStage(primaryStage);
    }

}
