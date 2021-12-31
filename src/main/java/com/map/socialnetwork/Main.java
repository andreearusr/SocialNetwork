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
import com.map.socialnetwork.service.config.ApplicationContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class Main extends Application {
    private Authenticator authenticator;
    private Service service;

    @Override
    public void start(Stage primaryStage) throws IOException {
        String url = ApplicationContext.getPROPERTIES().getProperty("db.url");
        String username = ApplicationContext.getPROPERTIES().getProperty("db.login.username");
        String password = ApplicationContext.getPROPERTIES().getProperty("db.login.password");

        System.out.println(url);
        System.out.println(username);
        System.out.println(password);

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