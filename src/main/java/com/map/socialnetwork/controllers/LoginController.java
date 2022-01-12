package com.map.socialnetwork.controllers;

import com.map.socialnetwork.Main;
import com.map.socialnetwork.exceptions.AuthenticationException;
import com.map.socialnetwork.service.Authenticator;
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
    private Authenticator authenticator;
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

    public void setAuthentication(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void tryLogin(String username, String password) {
        try {
            authenticator.logIn(username, password);
            changeScene();
        } catch (AuthenticationException | IOException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    private void changeScene() throws IOException, AuthenticationException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("userLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Go SOcial");
        primaryStage.setScene(scene);

        UserController userController = fxmlLoader.getController();
        userController.setAuthentication(authenticator);
        userController.setService(service);
        userController.setStage(primaryStage);
    }

    @FXML
    private void handleRegister() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("register.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Register");
        primaryStage.setScene(scene);

        RegisterController registerController = fxmlLoader.getController();
        registerController.setService(service);
        registerController.setAuthentication(authenticator);
        registerController.setStage(primaryStage);

    }

}
