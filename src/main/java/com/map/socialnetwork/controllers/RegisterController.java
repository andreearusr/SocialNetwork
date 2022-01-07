package com.map.socialnetwork.controllers;

import com.map.socialnetwork.Main;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.ValidatorException;
import com.map.socialnetwork.service.Authenticator;
import com.map.socialnetwork.service.Service;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    private Service service;
    private Stage primaryStage;
    private Authenticator authenticator;


    @FXML
    private TextField inputFirstname;

    @FXML
    private TextField inputLastname;

    @FXML
    private PasswordField inputPassword;

    @FXML
    private PasswordField inputConfirmPassword;

    @FXML
    private TextField inputUsername;

    @FXML
    private Label labelRegistration;

    @FXML
    private Label labelWrongPassword;


    public void setService(Service service) {
        this.service = service;
    }

    public void setAuthentication(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleRegister() {
        String password = inputPassword.getText();
        String confirmPassword = inputConfirmPassword.getText();

        if (password.equals(confirmPassword)) {
            registerUser();
            labelWrongPassword.setText("");
            labelRegistration.setText("User has been registred succesfully!");
        }
            else
            labelWrongPassword.setText("Password does not match");
        }

    private void registerUser() {
        String firstName = inputFirstname.getText();
        String lastName = inputLastname.getText();
        String username = inputUsername.getText();
        String password = inputPassword.getText();

        try {
            service.addUser(firstName, lastName);
            long id = service.getUser(firstName, lastName).get().getId();
            authenticator.addCredentials(id, username, password);
        } catch (ValidatorException | DuplicateEntityException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }

    }


    @FXML
    private void handleClose() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Social Network!");
        primaryStage.setScene(scene);

        LoginController loginController = fxmlLoader.getController();
        loginController.setAuthentication(authenticator);
        loginController.setService(service);
        loginController.setStage(primaryStage);

    }
}
