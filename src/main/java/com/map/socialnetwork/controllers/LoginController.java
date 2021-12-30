package com.map.socialnetwork.controllers;

import com.map.socialnetwork.Main;
import com.map.socialnetwork.exceptions.AuthenticationException;
import com.map.socialnetwork.service.Authentication;
import com.map.socialnetwork.service.Service;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    private Service service;
    private Authentication authentication;
    private Stage primaryStage;

    @FXML
    private TextField inputUsername;

    @FXML
    private PasswordField inputPassword;

    @FXML
    public void userLogin() {
        tryLogin(inputUsername.getText(), inputPassword.getText());
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

    private void tryLogin(String username, String password) {
        try {
            authentication.logIn(username, password);
            changeScene();
        } catch (AuthenticationException | IOException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    private void changeScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("userLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Social Network!");
        primaryStage.setScene(scene);

        UserController userController = fxmlLoader.getController();
        userController.setAuthentication(authentication);
        userController.setService(service);
        userController.setStage(primaryStage);
    }

}
