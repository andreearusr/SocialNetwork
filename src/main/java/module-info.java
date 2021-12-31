module com.map.socialnetwork {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires java.sql;
    requires lombok;

    opens com.map.socialnetwork to javafx.fxml;
    exports com.map.socialnetwork;
    exports com.map.socialnetwork.controllers;
    exports com.map.socialnetwork.service;
    exports com.map.socialnetwork.repository;
    exports com.map.socialnetwork.domain;
    exports com.map.socialnetwork.exceptions;
    exports com.map.socialnetwork.domain.validator;
    opens com.map.socialnetwork.controllers to javafx.fxml;
    opens com.map.socialnetwork.service to javafx.fxml;
    opens com.map.socialnetwork.domain.validator to javafx.fxml;
}