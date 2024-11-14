package com.agrotech.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.shape.Circle;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {
    @FXML private BorderPane mainContainer;
    @FXML private VBox topPanel;
    @FXML private Label dateLabel;
    @FXML private ComboBox<String> cropTypeCombo;
    @FXML private Circle statusIndicator;
    @FXML private Label systemStatusLabel;
    @FXML private Label lastUpdateLabel;
    @FXML private Label cropTypeLabel;

    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        setupTopPanel();
        setupMainPanel();
        setupBottomPanel();
        setupDateTime();
    }

    private void setupTopPanel() {
        updateDateTime();
        setupCropInfo();
        updateSystemStatus();
        setupLastUpdate();
    }

    private void setupDateTime() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDateTime()));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();

    }

    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = LocalDateTime.now().format(formatter);
        dateLabel.setText("Fecha: " + formattedDate);
    }

    private void setupCropInfo() {
        cropTypeCombo.getItems().addAll(
                "Oregano"
        );

        cropTypeCombo.setPromptText("Seleccionar cultivo");
        cropTypeCombo.setOnAction(e -> onCropTypeChanged());
    }

    private void onCropTypeChanged() {
        String selectedCrop = cropTypeCombo.getValue();
        if(selectedCrop != null) {
            updateSystemStatus();
        }
    }

    private void updateSystemStatus() {
        boolean isSystemOk = true;

        if(isSystemOk) {
            statusIndicator.getStyleClass().removeAll("warning", "error");
            statusIndicator.getStyleClass().add("success");
            systemStatusLabel.setText("Sistema Operativo");
        } else {
            statusIndicator.getStyleClass().removeAll("success", "error");
            statusIndicator.getStyleClass().add("error");
            systemStatusLabel.setText("Error en Sistema");
        }
    }

    private void setupLastUpdate() {
        updateLastUpdateTime();
    }

    private void updateLastUpdateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String time = LocalDateTime.now().format(formatter);
        lastUpdateLabel.setText(time);
    }

    private void setupMainPanel() {

    }

    private void setupBottomPanel() {

    }

    public void stop() {
        if(clockTimeline != null) {
            clockTimeline.stop();
        }
    }
}
