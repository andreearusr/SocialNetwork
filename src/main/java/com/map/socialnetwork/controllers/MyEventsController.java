package com.map.socialnetwork.controllers;

import com.map.socialnetwork.domain.Event;
import com.map.socialnetwork.domain.User;
import com.map.socialnetwork.repository.paging.Page;
import com.map.socialnetwork.repository.paging.PageableImpl;
import com.map.socialnetwork.service.Service;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;


public class MyEventsController {

    private Service service;
    private User myUser;

    private final ObservableList<Event> modelEvents = FXCollections.observableArrayList();
    private Page<Event> firstLoadedEvent;
    private Page<Event> secondLoadedEvent;

    @FXML
    private TableColumn<Event, String> tableColumnName;

    @FXML
    private TableColumn<Event, String> tableColumnDate;

    @FXML
    private TableView<Event> myEventsTable;


    public void setService(Service service) {
        this.service = service;
    }

    public void setUser(User user) {
        this.myUser = user;
        initModel();
    }


    @FXML
    private void initialize(){

        tableColumnName.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        tableColumnDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        myEventsTable.setItems(modelEvents);
        Platform.runLater(() -> {
            ScrollBar tvScrollBar = (ScrollBar) myEventsTable.lookup(".scroll-bar:vertical");
            tvScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((Double) newValue == 0.0) {
                    if (firstLoadedEvent.getPageable().getPageNumber() > 1) {
                        secondLoadedEvent = firstLoadedEvent;
                        firstLoadedEvent = service.getAttendingEvents(firstLoadedEvent.previousPageable(), myUser.getId());
                        setModel();
                    }
                } else if ((Double) newValue == 1.0) {
                    if (secondLoadedEvent.getContent().size() == secondLoadedEvent.getPageable().getPageSize()) {
                        Page<Event> newEvents = service.getAttendingEvents(secondLoadedEvent.nextPageable(), myUser.getId());

                        if (!newEvents.getContent().isEmpty()) {
                            firstLoadedEvent = secondLoadedEvent;
                            secondLoadedEvent = newEvents;
                            setModel();
                        }
                    }
                }
            });
        });
    }

    private void initModel() {
        firstLoadedEvent = service.getAttendingEvents(new PageableImpl<>(1, 7), myUser.getId());
        secondLoadedEvent = service.getAttendingEvents(new PageableImpl<>(2, 7), myUser.getId());

        setModel();
    }

    private void setModel() {
        List<Event> loadedEvents = firstLoadedEvent.getContent();
        loadedEvents.addAll(secondLoadedEvent.getContent());
        modelEvents.clear();
        modelEvents.setAll(loadedEvents);
    }



}
