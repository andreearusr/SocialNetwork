package com.map.socialnetwork;

import com.map.socialnetwork.repository.FriendshipRepository;
import com.map.socialnetwork.repository.MessageRepository;
import com.map.socialnetwork.repository.UserRepository;
import com.map.socialnetwork.service.Service;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        int width = (int) Screen.getPrimary().getBounds().getWidth();
        int height = (int) Screen.getPrimary().getBounds().getHeight();

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), width, height);
        stage.setTitle("Social Network!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
//        launch();
        String URL = "jdbc:postgresql://localhost:5432/social_network_v2";
        String username = "postgres";
        String password = "Database1";
        UserRepository userRepository = new UserRepository(URL, username, password);
        FriendshipRepository friendshipRepository = new FriendshipRepository(URL, username, password, userRepository);
        MessageRepository messageRepository = new MessageRepository(URL, username, password, userRepository);
        Service service = new Service(userRepository, friendshipRepository, messageRepository);
        service.getUsers().forEach(System.out::println);
    }
}