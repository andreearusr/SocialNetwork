package com.map.socialnetwork;

import com.map.socialnetwork.controllers.LoginController;
import com.map.socialnetwork.domain.Credentials;
import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.Message;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.domain.validator.*;
import com.map.socialnetwork.repository.CredentialsDBRepository;
import com.map.socialnetwork.repository.FriendshipDBRepository;
import com.map.socialnetwork.repository.MessageDBRepository;
import com.map.socialnetwork.repository.UserDBRepository;
import com.map.socialnetwork.service.Authenticator;
import com.map.socialnetwork.service.Service;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static final String url = "jdbc:postgresql://localhost:5432/social_network_v2";
    private static final String username = "postgres";
    private static final String password = "Database1";

    private Authenticator authenticator;
    private Service service;

    @Override
    public void start(Stage primaryStage) throws IOException {
        Validator<Credentials> credentialsValidator = new CredentialsValidator();
        Validator<User> userValidator = new UserValidator();
        Validator<Message> messageValidator = new MessageValidator();
        Validator<Friendship> friendshipValidator = new FriendshipValidator();

        CredentialsDBRepository credentialsDBRepository = new CredentialsDBRepository(url, username, password, credentialsValidator);
        UserDBRepository userDBRepository = new UserDBRepository(url, username, password, userValidator);
        MessageDBRepository messageDBRepository = new MessageDBRepository(url, username, password, userDBRepository, messageValidator);
        FriendshipDBRepository friendshipDBRepository = new FriendshipDBRepository(url, username, password, userDBRepository, friendshipValidator);
        authenticator = new Authenticator(credentialsDBRepository);
        service = new Service(userDBRepository, friendshipDBRepository, messageDBRepository);

        initView(primaryStage);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Meta");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private void initView(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 347, 320);
        primaryStage.setTitle("Social Network!");
        primaryStage.setScene(scene);

        LoginController loginController = fxmlLoader.getController();
        loginController.setAuthentication(authenticator);
        loginController.setService(service);
        loginController.setStage(primaryStage);
    }
}