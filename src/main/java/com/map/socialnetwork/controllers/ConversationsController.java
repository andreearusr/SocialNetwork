package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Friendship;
import com.map.socialnetwork.domain.Message;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.MissingEntityException;
import com.map.socialnetwork.exceptions.ValidatorException;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.PageableImpl;
import com.map.socialnetwork.service.Service;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

public class ConversationsController implements Observer {
    private Service service;
    private User myUser;

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

    private Page<User> firstLoadedUsersPage;
    private Page<User> secondLoadedUsersPage;

    private Page<Message> firstLoadedMessagesPage;
    private Page<Message> secondLoadedMessagesPage;

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
        selectedUser.addListener((ListChangeListener<User>) c -> initMessages());
        initModel();
        usersTable.getSelectionModel().select(0);
    }

    public void setUser(User user) {
        this.myUser = user;
    }

    @FXML
    private void initialize() {
        user.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        from.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFrom().getFullName()));
        to.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTo()
                .stream().map(User::getFullName).collect(Collectors.joining(", "))));
        time.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        message.setCellValueFactory(new PropertyValueFactory<>("message"));
        replyTo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReply() == null
                ? "" : cellData.getValue().getReply().getMessage()));

        usersTable.setItems(usersModel);
        messages.setItems(messagesModel);

        addUsersTableScrollbarListener();
        addMessagesTableScrollbarListener();
    }

    private void addUsersTableScrollbarListener() {
        Platform.runLater(() -> {
            ScrollBar tvScrollBar = (ScrollBar) usersTable.lookup(".scroll-bar:vertical");
            tvScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((Double) newValue == 0.0) {
                    if (firstLoadedUsersPage.getPageable().getPageNumber() > 1) {
                        secondLoadedUsersPage = firstLoadedUsersPage;
                        firstLoadedUsersPage = service.getUsers(firstLoadedUsersPage.previousPageable());
                        setUsersModel();
                    }
                } else if ((Double) newValue == 1.0) {
                    if (secondLoadedUsersPage.getContent().size() == secondLoadedUsersPage.getPageable().getPageSize()) {
                        Page<User> newUsers = service.getUsers(secondLoadedUsersPage.nextPageable());

                        if (!newUsers.getContent().isEmpty()) {
                            firstLoadedUsersPage = secondLoadedUsersPage;
                            secondLoadedUsersPage = newUsers;
                            setUsersModel();
                        }
                    }
                }
            });
        });
    }

    private void addMessagesTableScrollbarListener() {
        Platform.runLater(() -> {
            ScrollBar tvScrollBar = (ScrollBar) messages.lookup(".scroll-bar:vertical");
            tvScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((Double) newValue == 0.0) {
                    if (firstLoadedMessagesPage.getPageable().getPageNumber() > 1) {
                        if (!selectedUser.isEmpty()) {
                            try {
                                secondLoadedMessagesPage = firstLoadedMessagesPage;
                                firstLoadedMessagesPage = service.getConversation(firstLoadedMessagesPage.previousPageable(),
                                        myUser.getId(), selectedUser.get(0).getId());
                                setMessagesModel();
                            } catch (MissingEntityException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if ((Double) newValue == 1.0) {
                    if (secondLoadedMessagesPage.getContent().size() == secondLoadedMessagesPage.getPageable().getPageSize()) {
                        if (!selectedUser.isEmpty()) {
                            try {
                                Page<Message> newUsers = service.getConversation(secondLoadedMessagesPage.nextPageable(),
                                        myUser.getId(), selectedUser.get(0).getId());

                                if (!newUsers.getContent().isEmpty()) {
                                    firstLoadedMessagesPage = secondLoadedMessagesPage;
                                    secondLoadedMessagesPage = newUsers;
                                    setMessagesModel();
                                }
                            } catch (MissingEntityException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        });
    }

    private void initModel() {
        initUsers();
        initMessages();
    }

    private void initUsers() {
        firstLoadedUsersPage = service.getUsers(new PageableImpl<>(1, 20));
        secondLoadedUsersPage = service.getUsers(new PageableImpl<>(2, 20));

        setUsersModel();
    }

    private void setUsersModel() {
        List<User> users = firstLoadedUsersPage.getContent();
        users.addAll(secondLoadedUsersPage.getContent());
        users.removeIf(user -> user.getId().equals(myUser.getId()));
        usersModel.setAll(users);
    }

    private void initMessages() {
        try {
            if (!selectedUser.isEmpty()) {
                firstLoadedMessagesPage = service.getConversation(new PageableImpl<>(1, 20),
                        myUser.getId(), selectedUser.get(0).getId());
                secondLoadedMessagesPage = service.getConversation(new PageableImpl<>(2, 20),
                        myUser.getId(), selectedUser.get(0).getId());
                setMessagesModel();
            }
        } catch (MissingEntityException e) {
            e.printStackTrace();
        }
    }

    private void setMessagesModel() {
        if (selectedUser.isEmpty()) {
            messagesModel.clear();
        } else {

            messagesModel.setAll(firstLoadedMessagesPage.getContent());
            messagesModel.addAll(secondLoadedMessagesPage.getContent());
        }
    }

    @FXML
    private void sendMessage() {
        if (inputMessage.getText().length() == 0) {
            MessageAlert.showErrorMessage(null, "Message is empty!");
            return;
        }

        if (messages.getSelectionModel().isEmpty()) {
            try {
                service.sendSingleMessage(inputMessage.getText(), List.of(selectedUser.get(0).getId()), myUser.getId());
            } catch (ValidatorException | DuplicateEntityException e) {
                MessageAlert.showErrorMessage(null, e.getMessage());
                e.printStackTrace();
            }
        } else {
            if (!replyAll.isSelected()) {
                try {
                    service.replyMessage(inputMessage.getText(), myUser.getId(), selectedUser.get(0).getId(),
                            messages.getSelectionModel().getSelectedItem().getId());
                } catch (MissingEntityException | ValidatorException | DuplicateEntityException e) {
                    MessageAlert.showErrorMessage(null, e.getMessage());
                    e.printStackTrace();
                }
            } else {
                try {
                    service.replyAllMessage(inputMessage.getText(), myUser.getId(), messages.getSelectionModel()
                            .getSelectedItem().getId());
                } catch (MissingEntityException | ValidatorException | DuplicateEntityException e) {
                    MessageAlert.showErrorMessage(null, e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        inputMessage.clear();
    }
}
