package com.map.socialnetwork.controllers;

import com.map.socialnetwork.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField inputUsername;

    @FXML
    private PasswordField inputPassword;

    @FXML
    private Button loginButton;

    public void userLogin(ActionEvent event) throws IOException {
        tryLogin(inputUsername.getText(), inputPassword.getText());
    }

    private void tryLogin(String username, String password) throws IOException {
        Main main = new Main();

        //TODO CHECK CREDENTIALS
        if(true) {
            main.changeScene("main.fxml");
        } else {
            //TODO PRINT MESSAGE
        }
    }
}
