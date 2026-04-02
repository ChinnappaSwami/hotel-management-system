package com.hotel.management;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/management/main-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1100, 760);

        // Load the CSS stylesheet
        String css = getClass().getResource("/com/hotel/management/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Hotel Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
