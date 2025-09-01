package com.example.worldwise.worldwise;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Database.initializeDatabase();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Layout.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Apply CSS globally
        String stylesheet = Objects.requireNonNull(HelloApplication.class.getResource("stylesheet.css")).toExternalForm();
        scene.getStylesheets().add(stylesheet);

        stage.setTitle("World Wise");
        stage.setScene(scene);

        // Set fullscreen
        stage.setMaximized(true);

        stage.show();
    }
}
