package com.agrotech.handler;


import com.agrotech.model.ExportData;
import com.agrotech.service.ExportService;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ExportHandler {
    private final ExportService exportService;

    public ExportHandler() {
        this.exportService = new ExportService();
    }

    public void exportToSQL(Window window, ExportData data) {
        try {
            String content = exportService.generateSQLScript(data);
            saveToFile(window, content, "Script SQL", "sql");
        } catch (Exception e) {
            showAlert("Error", "Error al generar script SQL: " + e.getMessage());
        }
    }

    public void exportToNoSQL(Window window, ExportData data) {
        try {
            String content = exportService.generateNoSQLScript(data);
            saveToFile(window, content, "Script MongoDB", "js");
        } catch (Exception e) {
            showAlert("Error", "Error al generar script NoSQL: " + e.getMessage());
        }
    }

    public void exportToJSON(Window window, ExportData data) {
        try {
            String content = exportService.generateJSONData(data);
            saveToFile(window, content, "Datos JSON", "json");
        } catch (Exception e) {
            showAlert("Error", "Error al generar JSON: " + e.getMessage());
        }
    }

    private void saveToFile(Window window, String content, String description, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar " + description);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(description, "*." + extension)
        );

        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            try {
                Files.writeString(file.toPath(), content);
                showAlert("Éxito", "Archivo generado correctamente en:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Error", "Error al guardar el archivo: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert.AlertType type = title.equals("Error") ?
                Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
