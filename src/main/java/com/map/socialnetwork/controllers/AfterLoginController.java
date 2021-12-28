package com.map.socialnetwork.controllers;

import com.map.socialnetwork.service.Authentication;
import com.map.socialnetwork.service.Service;
import javafx.stage.Stage;

public class AfterLoginController {
    private Authentication authentication;
    private Service service;
    private Stage primaryStage;

    public void setService(Service service) {
        this.service = service;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
