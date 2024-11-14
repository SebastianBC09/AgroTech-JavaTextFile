package com.agrotech.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {
    @FXML private BorderPane mainContainer;
    @FXML private VBox topPanel;
    @FXML private Label dateLabel;
    @FXML private Label cropTypeLabel;
    @FXML private Label systemStatusLabel;

    @FXML
    public void initialize() {
        setupTopPanel();
        setupMainPanel();
        setupBottomPanel();
    }

    private void setupTopPanel() {
        updateDateTime();
        setupCropInfo();
        updateSystemStatus();
    }

    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = LocalDateTime.now().format(formatter);
        dateLabel.setText("Fecha: " + formattedDate);
    }

    private void setupCropInfo() {

    }

    private void updateSystemStatus() {

    }

    private void setupMainPanel() {

    }

    private void setupBottomPanel() {

    }
}
