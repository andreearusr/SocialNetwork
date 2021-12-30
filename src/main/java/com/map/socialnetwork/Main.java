package com.map.socialnetwork;

import com.map.socialnetwork.controllers.LoginController;
import com.map.socialnetwork.repository.CredentialsRepository;
import com.map.socialnetwork.repository.FriendshipRepository;
import com.map.socialnetwork.repository.MessageRepository;
import com.map.socialnetwork.repository.UserRepository;
import com.map.socialnetwork.service.Authentication;
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

    private Authentication authentication;
    private Service service;

    @Override
    public void start(Stage primaryStage) throws IOException {
        CredentialsRepository credentialsRepository = new CredentialsRepository(url, username, password);
        UserRepository userRepository = new UserRepository(url, username, password);
        MessageRepository messageRepository = new MessageRepository(url, username, password, userRepository);
        FriendshipRepository friendshipRepository = new FriendshipRepository(url, username, password, userRepository);
        authentication = new Authentication(credentialsRepository);
        service = new Service(userRepository, friendshipRepository, messageRepository);

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
        loginController.setAuthentication(authentication);
        loginController.setService(service);
        loginController.setStage(primaryStage);
    }
}