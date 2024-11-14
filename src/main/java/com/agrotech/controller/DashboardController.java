package com.agrotech.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
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
    @FXML private TextField tempInput;
    @FXML private ComboBox<String> tempUnitCombo;
    @FXML private TextField volInput;
    @FXML private ComboBox<String> volUnitCombo;
    @FXML private ComboBox<String> containerTypeCombo;
    @FXML private Spinner<Integer> containerCountSpinner;
    @FXML private Slider approximateVolSlider;
    @FXML private Label sliderLabel;

    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        setupTopPanel();
        setupMainPanel();
        setupBottomPanel();
        setupDateTime();
        setupCenterPanel();
    }

    private void setupTopPanel() {
        updateDateTime();
        setupCropInfo();
        updateSystemStatus();
        setupLastUpdate();
    }

    private void setupCenterPanel() {
        setupTemperatureControls();
        setupVolumeControls();
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

    private void setupTemperatureControls() {
        tempUnitCombo.getItems().addAll("°C", "°F");
        tempUnitCombo.setValue("°C");

        tempInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*\\.?\\d*")) {
                tempInput.setText(oldValue);
            }
        });
    }

    private void setupVolumeControls() {
        volUnitCombo.getItems().addAll("L", "mL", "m³");
        volUnitCombo.setValue("L");

        containerTypeCombo.getItems().addAll(
                "Balde (20L)",
                "Tanque (200L)",
                "Bidón (5L)"
        );

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100,1);
        containerCountSpinner.setValueFactory(valueFactory);

        approximateVolSlider.setMin(0);
        approximateVolSlider.setMax(100);
        approximateVolSlider.setValue(50);

        approximateVolSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateSliderLabel(newValue.doubleValue());
        });

        updateSliderLabel(50);
    }

    private void updateSliderLabel(double value) {
        String description;
        if(value < 33) {
            description = "Riego Ligero";
        } else if(value < 66) {
            description = "Riego Moderado";
        } else {
            description = "Riego Abundante";
        }
        sliderLabel.setText(description + String.format(" (%.0f%%)", value));
    }

    @FXML
    private void setTempFria() {
        tempInput.setText("15");
        tempUnitCombo.setValue("°C");
    }

    @FXML
    private void setTempTemplada() {
        tempInput.setText("20");
        tempUnitCombo.setValue("°C");
    }

    @FXML
    private void setTempCaliente() {
        tempInput.setText("35");
        tempUnitCombo.setValue("°C");
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
