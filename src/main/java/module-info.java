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
}