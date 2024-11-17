package com.agrotech.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class SceneController {

    private final Stage primaryStage;

    public SceneController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void switchToWelcome() throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/welcome-view.fxml")));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public void switchToDashboard() throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/dashboard-view.fxml")));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}