package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Message;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.MissingEntityException;
import com.map.socialnetwork.service.Authentication;
import com.map.socialnetwork.service.Service;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

public class ConversationsController implements Observer {

    private Service service;
    private Authentication authentication;
    private Long userId;

    private final ObservableList<User> usersModel = FXCollections.observableArrayList();
    private final ObservableList<Message> messagesModel = FXCollections.observableArrayList();

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> user;

    @FXML
    private TableView<Message> messages;

    @FXML
    private TableColumn<Message, String> from;

    @FXML
    private TableColumn<Message, String> to;

    @FXML
    private TableColumn<Message, Timestamp> time;

    @FXML
    private TableColumn<Message, String> message;

    @FXML
    private TableColumn<Message, String> replyTo;

    @FXML
    private CheckBox replyAll;

    @FXML
    private TextField inputMessage;

    private ObservableList<User> selectedUser;

    @Override
    public void update(Observable o, Object arg) {
        if (arg == Message.class) {
            initMessages();
        } else {
            initModel();
        }
    }

    public void setService(Service service) {
        this.service = service;
        service.addObserver(this);

        selectedUser = usersTable.getSelectionModel().getSelectedItems();
        selectedUser.addListener((ListChangeListener<User>) c -> {
            initMessages();
        });
        initModel();
        usersTable.getSelectionModel().select(0);
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
        this.userId = authentication.getUserId();
    }

    @FXML
    private void initialize() {
        user.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        from.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFrom().getFullName()));
        to.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTo()
                .stream().map(User::getFullName).collect(Collectors.joining(", "))));
        time.setCellValueFactory(new PropertyValueFactory<>("time"));
        message.setCellValueFactory(new PropertyValueFactory<>("message"));
        replyTo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReply() == null
        ? "" : cellData.getValue().getReply().getMessage()));

        usersTable.setItems(usersModel);
        messages.setItems(messagesModel);
    }

    private void initModel() {
        initUsers();
        initMessages();
    }

    private void initUsers() {
        List<User> users = service.getUsers();
        users.removeIf(user -> user.getId().equals(userId));
        usersModel.setAll(users);
    }

    private void initMessages() {
        try {
            if (selectedUser.isEmpty()) {
                messagesModel.clear();
            } else {
                messagesModel.setAll(service.getConversation(userId, selectedUser.get(0).getId()));
            }
        } catch (MissingEntityException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendMessage() {
        if (inputMessage.getText().length() == 0) {
            MessageAlert.showErrorMessage(null, "Message is empty!");
            return;
        }

        if (messages.getSelectionModel().isEmpty()) {
            service.sendSingleMessage(inputMessage.getText(), List.of(selectedUser.get(0).getId()), userId);
        } else {
            if (!replyAll.isSelected()) {
                try {
                    service.replyMessage(inputMessage.getText(), userId, selectedUser.get(0).getId(),
                            messages.getSelectionModel().getSelectedItem().getId());
                } catch (MissingEntityException e) {
                    MessageAlert.showErrorMessage(null, e.getMessage());
                    e.printStackTrace();
                }
            } else {
                try {
                    service.replyAllMessage(inputMessage.getText(), userId, messages.getSelectionModel()
                            .getSelectedItem().getId());
                } catch (MissingEntityException e) {
                    MessageAlert.showErrorMessage(null, e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        inputMessage.clear();
    }
}
