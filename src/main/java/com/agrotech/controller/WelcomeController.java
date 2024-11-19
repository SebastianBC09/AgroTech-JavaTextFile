package com.agrotech.controller;

import com.agrotech.exception.CSVProcessingException;
import com.agrotech.exception.FileValidationException;

import com.agrotech.model.UploadState;
import com.agrotech.service.CSVProcessingService;
import com.agrotech.service.DataTransformationService;
import com.agrotech.service.FileValidationService;

import com.agrotech.service.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;

public class WelcomeController {
    @FXML private VBox dropZone;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;

    private final FileValidationService validationService;
    private final CSVProcessingService processingService;

    public WelcomeController() {
        this.validationService = new FileValidationService();
        this.processingService = new CSVProcessingService();
    }

    @FXML
    public void initialize() {
        setupDragAndDrop();
        updateState(UploadState.INITIAL);
    }

    private void setupDragAndDrop() {
        dropZone.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                updateState(UploadState.DRAGGING);
            }
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            var db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles() && !db.getFiles().isEmpty()) {
                handleFileSelection(db.getFiles().getFirst());
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

        dropZone.setOnDragExited(event -> {
            updateState(UploadState.INITIAL);
            event.consume();
        });
    }


    private void handleFileSelection(File file) {
        try {
            if (!validationService.validateFile(file)) {
                showError("El archivo no es válido");
                return;
            }

            processFile(file);

        } catch (FileValidationException e) {
            showError(e.getMessage());
        }
    }

    private void processFile(File file) {
        updateState(UploadState.PROCESSING);

        Thread processThread = new Thread(() -> {
            try {
                boolean success = processingService.processCSVFile(file, progress ->
                        Platform.runLater(() -> progressBar.setProgress(progress))
                );

                Platform.runLater(() -> {
                    if (success) {
                        try {
                            // Enriquecer los datos
                            DataTransformationService.getInstance()
                                    .enrichSensorData(processingService.getProcessedData());

                            // Navegar al dashboard
                            Stage stage = (Stage) dropZone.getScene().getWindow();
                            NavigationService.getInstance().navigateToDashboard(stage);
                        } catch (Exception e) {
                            updateState(UploadState.ERROR);
                            showError("Error al cambiar de vista: " + e.getMessage());
                        }
                    } else {
                        updateState(UploadState.ERROR);
                        showError("Error al procesar el archivo");
                    }
                });

            } catch (CSVProcessingException e) {
                Platform.runLater(() -> {
                    updateState(UploadState.ERROR);
                    showError(e.getMessage());
                });
            }
        });

        processThread.setDaemon(true);
        processThread.start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateState(UploadState state) {
        String style = state.getStyle();
        String message = state.getMessage();

        dropZone.setStyle(style);
        statusLabel.setText(message);
        progressBar.setVisible(state == UploadState.PROCESSING);
        dropZone.setDisable(state == UploadState.PROCESSING);
    }
}

