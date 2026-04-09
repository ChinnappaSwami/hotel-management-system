module com.hotel.management {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;

    exports com.hotel.management;
    exports com.hotel.management.controller;
    exports com.hotel.management.model;
    exports com.hotel.management.service;
    exports com.hotel.management.cleaning;

    opens com.hotel.management to javafx.fxml;
    opens com.hotel.management.controller to javafx.fxml;
    opens com.hotel.management.model to javafx.base;
    opens com.hotel.management.service to javafx.fxml;
}

