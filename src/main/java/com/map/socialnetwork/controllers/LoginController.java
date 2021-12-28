package com.map.socialnetwork.controllers;

import com.map.socialnetwork.Main;
import com.map.socialnetwork.exceptions.AuthenticationException;
import com.map.socialnetwork.service.Authentication;
import com.map.socialnetwork.service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    Service service;
    Authentication authentication;
    Stage primaryStage;

    @FXML
    private TextField inputUsername;

    @FXML
    private PasswordField inputPassword;

    @FXML
    private Button loginButton;

    public void userLogin(ActionEvent event) throws IOException {
        tryLogin(inputUsername.getText(), inputPassword.getText());
    }

    private void tryLogin(String username, String password) {
        try {
            authentication.logIn(username, password);
            changeScene();
        } catch (AuthenticationException | IOException e) {
            //TODO PRINT MESSAGE
        }
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void changeScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("afterLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
        primaryStage.setTitle("Social Network!");
        primaryStage.setScene(scene);

        AfterLoginController afterLoginController = fxmlLoader.getController();
        afterLoginController.setService(service);
        afterLoginController.setAuthentication(authentication);
        afterLoginController.setStage(primaryStage);
    }
}