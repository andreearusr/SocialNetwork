package com.map.socialnetwork;

import com.map.socialnetwork.controllers.LoginController;
import com.map.socialnetwork.domain.*;
import com.map.socialnetwork.domain.validator.*;
import com.map.socialnetwork.repository.credentialsRepository.CredentialsDBRepository;
import com.map.socialnetwork.repository.eventRepository.EventDBRepository;
import com.map.socialnetwork.repository.eventRepository.EventRepository;
import com.map.socialnetwork.repository.friendshipRepository.FriendshipDBRepository;
import com.map.socialnetwork.repository.friendshipRepository.FriendshipRepository;
import com.map.socialnetwork.repository.messageRepository.MessageDBRepository;
import com.map.socialnetwork.repository.messageRepository.MessageRepository;
import com.map.socialnetwork.repository.userRepository.UserDBRepository;
import com.map.socialnetwork.repository.userRepository.UserRepository;
import com.map.socialnetwork.service.Authenticator;
import com.map.socialnetwork.service.Service;
import com.map.socialnetwork.service.config.ApplicationContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private Authenticator authenticator;
    private Service service;

    @Override
    public void start(Stage primaryStage) throws IOException {
        String url = ApplicationContext.getPROPERTIES().getProperty("db.url");
        String username = ApplicationContext.getPROPERTIES().getProperty("db.login.username");
        String password = ApplicationContext.getPROPERTIES().getProperty("db.login.password");

        Validator<Credentials> credentialsValidator = new CredentialsValidator();
        Validator<User> userValidator = new UserValidator();
        Validator<Message> messageValidator = new MessageValidator();
        Validator<Friendship> friendshipValidator = new FriendshipValidator();
        Validator<Event> eventValidator = new EventsValidator();

        CredentialsDBRepository credentialsDBRepository = new CredentialsDBRepository(url, username, password, credentialsValidator);
        UserRepository userDBRepository = new UserDBRepository(url, username, password, userValidator);
        MessageRepository messageDBRepository = new MessageDBRepository(url, username, password, userDBRepository, messageValidator);
        FriendshipRepository friendshipDBRepository = new FriendshipDBRepository(url, username, password, userDBRepository, friendshipValidator);
        EventRepository eventDBRepository = new EventDBRepository(url, username, password, userDBRepository, eventValidator);
        authenticator = new Authenticator(credentialsDBRepository);
        service = new Service(userDBRepository, friendshipDBRepository, messageDBRepository, eventDBRepository);

        initView(primaryStage);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Go SOcial");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private void initView(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Go SOcial");
        primaryStage.setScene(scene);

        LoginController loginController = fxmlLoader.getController();
        loginController.setAuthentication(authenticator);
        loginController.setService(service);
        loginController.setStage(primaryStage);
    }
}