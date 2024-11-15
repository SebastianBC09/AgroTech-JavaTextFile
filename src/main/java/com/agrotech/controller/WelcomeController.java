package com.agrotech.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

public class WelcomeController {

    @FXML
    private Label statusLabel;
    @FXML
    private VBox dropZone;
    @FXML
    private ProgressBar progressBar;

    public enum uploadState{
        INITIAL , DRAGGING , PROCESSING , RESULT
    }

    public void initialize(){
        dropZone.setOnDragOver(this::handleDragOver);
        dropZone.setOnDragDropped(this::handleDragDropped);
    }

    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
            System.out.println("ARCHIVO ARASTRADO");
            event.acceptTransferModes(TransferMode.COPY);
            updateVisualState(uploadState.DRAGGING);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
            var files = event.getDragboard().getFiles();
            try {
                File file = files.get(0);
                if (isValidFile(file)) {
                    updateVisualState(uploadState.PROCESSING);
                    processFile(file);
                } else {
                    handleError(new FileProcessingException("ARCHIVO NO VALIDO"));
                }
            } catch (FileProcessingException e) {
                handleError(e);
            }
        }
        event.setDropCompleted(true);
        event.consume();
    }

    private boolean isValidFile(File file) throws FileProcessingException {
        if (!file.exists()) {
            throw new FileProcessingException("EL ARCHIVO NO EXISTE");
        }
        if (file.length() == 0) {
            throw new FileProcessingException("EL ARCHIVO ESTA VACIO");
        }
        return file.getName().endsWith(".csv");
    }

    private void processFile(File file) {
        new Thread(() -> {
            try {
                for (int i = 0; i <= 100; i++) {
                    final int progress = i;
                    Platform.runLater(() -> progressBar.setProgress(progress / 100.0));
                    Thread.sleep(50);
                    System.out.println("PROCESANDO: " + progress + "%");
                }
                Platform.runLater(() -> showResult(true));
            } catch (InterruptedException e) {
                Platform.runLater(() -> handleError(new FileProcessingException("Error durante el procesamiento del archivo")));
            }
        }).start();
    }

    private void showResult(boolean success) {
        if (success) {
            updateVisualState(uploadState.RESULT);
            statusLabel.setText("ARCHIVO CARGADO DE FORMA CORECTA");
        } else {
            updateVisualState(uploadState.RESULT);
            statusLabel.setText("ERROR AL PROCESAR EL ARCHIVO");
        }
    }

    private void handleError(Exception e) {
        statusLabel.setText(e.getMessage());
        updateVisualState(uploadState.RESULT);

        System.err.println("Error: " + e.getMessage());
    }

    private void updateVisualState(uploadState state){
        switch(state){
            case INITIAL:
                dropZone.setStyle("-fx-background-color: lightgray;");
                statusLabel.setText("ARRASTRAR EL ARCHIVO AQUI");
                progressBar.setVisible(false);
                break;
            case DRAGGING:
                dropZone.setStyle("-fx-background-color: lightblue;");
                statusLabel.setText("SOLTAR AQUI");
                progressBar.setVisible(false);
                break;
            case PROCESSING:
                dropZone.setDisable(true);
                dropZone.setStyle("-fx-background-color: lightblue;");
                statusLabel.setText("VALIDANDO...");
                progressBar.setVisible(true);
                break;

            case RESULT:
                dropZone.setStyle("-fx-background-color: green;");
                progressBar.setVisible(false);
                break;
            default:
                break;

        }
    }
}

