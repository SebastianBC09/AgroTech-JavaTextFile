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
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    @FXML private Button sqlButton;
    @FXML private Button nosqlButton;
    @FXML private Button jsonButton;

    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        setupDateTime();
        setupTopPanel();
        setupCenterPanel();
        setupMainPanel();
        setupBottomPanel();
    }

    private void setupDateTime() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDateTime()));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    private void setupTopPanel() {
        updateDateTime();
        setupCropInfo();
        updateSystemStatus();
        updateLastUpdateTime();
    }

    private void setupCenterPanel() {
        setupTemperatureControls();
        setupVolumeControls();
    }

    private void setupMainPanel() {
        // Configurar tooltips para mejor comprensión
        tempInput.setTooltip(new Tooltip("Temperatura del agua utilizada"));
        tempUnitCombo.setTooltip(new Tooltip("Unidad de temperatura"));
        volInput.setTooltip(new Tooltip("Volumen de agua"));
        volUnitCombo.setTooltip(new Tooltip("Unidad de volumen"));
        approximateVolSlider.setTooltip(new Tooltip("Nivel aproximado de riego"));

        // Configurar conversiones automáticas
        containerTypeCombo.setOnAction(e -> updateContainerVolume());
        containerCountSpinner.valueProperty().addListener((obs, old, newVal) ->
                updateContainerVolume());
        volUnitCombo.setOnAction(e -> convertVolume());
    }

    private void setupBottomPanel() {
        // Configurar botones de exportación
        sqlButton.setOnAction(e -> exportToSQL());
        nosqlButton.setOnAction(e -> exportToNoSQL());
        jsonButton.setOnAction(e -> exportToJSON());
    }

    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = LocalDateTime.now().format(formatter);
        dateLabel.setText("Fecha: " + formattedDate);
    }

    private void setupCropInfo() {
        cropTypeCombo.getItems().addAll("Oregano");
        cropTypeCombo.setPromptText("Seleccionar cultivo");
        cropTypeCombo.setOnAction(e -> onCropTypeChanged());
    }

    private void onCropTypeChanged() {
        if (cropTypeCombo.getValue() != null) {
            updateSystemStatus();
        }
    }

    private void updateSystemStatus() {
        statusIndicator.getStyleClass().removeAll("warning", "error");
        statusIndicator.getStyleClass().add("success");
        systemStatusLabel.setText("Sistema Operativo");
    }

    private void updateLastUpdateTime() {
        lastUpdateLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private void setupTemperatureControls() {
        tempUnitCombo.getItems().addAll("°C", "°F");
        tempUnitCombo.setValue("°C");

        // Validación numérica
        tempInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("-?\\d*\\.?\\d*")) {
                tempInput.setText(oldValue);
            }
        });

        // Conversión automática
        tempUnitCombo.setOnAction(e -> convertTemperature());
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

    private void convertTemperature() {
        if (tempInput.getText().isEmpty()) return;
        try {
            double temp = Double.parseDouble(tempInput.getText());
            if (tempUnitCombo.getValue().equals("°C")) {
                tempInput.setText(String.format("%.1f", (temp - 32) * 5/9));
            } else {
                tempInput.setText(String.format("%.1f", temp * 9/5 + 32));
            }
        } catch (NumberFormatException ignored) {}
    }

    private void setupVolumeControls() {
        volUnitCombo.getItems().addAll("L", "mL", "m³");
        volUnitCombo.setValue("L");

        containerTypeCombo.getItems().addAll(
                "Balde (20L)",
                "Tanque (200L)",
                "Bidón (5L)"
        );

        containerCountSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1)
        );

        setupVolumeSlider();
    }

    private void setupVolumeSlider() {
        approximateVolSlider.setMin(0);
        approximateVolSlider.setMax(100);
        approximateVolSlider.setValue(50);
        approximateVolSlider.valueProperty().addListener(
                (obs, old, newVal) -> updateSliderLabel(newVal.doubleValue())
        );
        updateSliderLabel(50);
    }

    private void updateSliderLabel(double value) {
        String description = value < 33 ? "Riego Ligero" :
                value < 66 ? "Riego Moderado" :
                        "Riego Abundante";
        sliderLabel.setText(description + String.format(" (%.0f%%)", value));
    }

    private void updateContainerVolume() {
        String containerType = containerTypeCombo.getValue();
        if (containerType == null) return;

        double baseVolume = switch (containerType) {
            case "Balde (20L)" -> 20.0;
            case "Tanque (200L)" -> 200.0;
            case "Bidón (5L)" -> 5.0;
            default -> 0.0;
        };

        double totalVolume = baseVolume * containerCountSpinner.getValue();
        volInput.setText(String.format("%.2f", totalVolume));
        volUnitCombo.setValue("L");
    }

    private void convertVolume() {
        if (volInput.getText().isEmpty()) return;
        try {
            double volume = Double.parseDouble(volInput.getText());
            String unit = volUnitCombo.getValue();
            double converted = switch (unit) {
                case "mL" -> volume * 1000;
                case "m³" -> volume / 1000;
                default -> volume;
            };
            volInput.setText(String.format("%.2f", converted));
        } catch (NumberFormatException ignored) {}
    }

    private void exportToSQL() {
        try {
            StringBuilder sql = new StringBuilder();

            // Crear tabla si no existe
            sql.append("CREATE TABLE IF NOT EXISTS agricultural_records (\n")
                    .append("    id SERIAL PRIMARY KEY,\n")
                    .append("    record_date TIMESTAMP,\n")
                    .append("    crop_type VARCHAR(50),\n")
                    .append("    soil_humidity DECIMAL(5,2),\n")
                    .append("    air_temperature DECIMAL(5,2),\n")
                    .append("    air_humidity DECIMAL(5,2),\n")
                    .append("    water_temperature DECIMAL(5,2),\n")
                    .append("    water_temperature_unit VARCHAR(2),\n")
                    .append("    water_volume DECIMAL(10,2),\n")
                    .append("    water_volume_unit VARCHAR(5),\n")
                    .append("    container_type VARCHAR(50),\n")
                    .append("    container_count INTEGER,\n")
                    .append("    irrigation_level INTEGER\n")
                    .append(");\n\n");

            // Insertar datos
            sql.append("INSERT INTO agricultural_records\n")
                    .append("(record_date, crop_type, soil_humidity, air_temperature, air_humidity, ")
                    .append("water_temperature, water_temperature_unit, water_volume, water_volume_unit, ")
                    .append("container_type, container_count, irrigation_level)\n")
                    .append("VALUES\n(\n")
                    .append(String.format("    TIMESTAMP '%s',\n",
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .append(String.format("    '%s',\n", cropTypeCombo.getValue()))
                    .append("    0.00,\n") // soil_humidity del CSV
                    .append("    0.00,\n") // air_temperature del CSV
                    .append("    0.00,\n") // air_humidity del CSV
                    .append(String.format("    %.2f,\n", Double.parseDouble(tempInput.getText())))
                    .append(String.format("    '%s',\n", tempUnitCombo.getValue()))
                    .append(String.format("    %.2f,\n", Double.parseDouble(volInput.getText())))
                    .append(String.format("    '%s',\n", volUnitCombo.getValue()))
                    .append(String.format("    '%s',\n",
                            containerTypeCombo.getValue() != null ? containerTypeCombo.getValue() : ""))
                    .append(String.format("    %d,\n", containerCountSpinner.getValue()))
                    .append(String.format("    %.0f\n", approximateVolSlider.getValue()))
                    .append(");");

            saveToFile(sql.toString(), "SQL Script", "sql");

        } catch (Exception e) {
            showAlert("Error", "Error al generar script SQL: " + e.getMessage());
        }

    }

    private void exportToNoSQL() {
        try {
            StringBuilder nosql = new StringBuilder();

            // Crear documento MongoDB
            nosql.append("db.agricultural_records.insertOne({\n")
                    .append(String.format("    timestamp: ISODate(\"%s\"),\n",
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .append(String.format("    crop_type: \"%s\",\n", cropTypeCombo.getValue()))
                    .append("    sensor_data: {\n")
                    .append("        soil_humidity: 0.00,\n")    // Del CSV
                    .append("        air_temperature: 0.00,\n")  // Del CSV
                    .append("        air_humidity: 0.00\n")      // Del CSV
                    .append("    },\n")
                    .append("    irrigation_data: {\n")
                    .append("        water: {\n")
                    .append(String.format("            temperature: %.2f,\n",
                            Double.parseDouble(tempInput.getText())))
                    .append(String.format("            temperature_unit: \"%s\",\n",
                            tempUnitCombo.getValue()))
                    .append(String.format("            volume: %.2f,\n",
                            Double.parseDouble(volInput.getText())))
                    .append(String.format("            volume_unit: \"%s\"\n",
                            volUnitCombo.getValue()))
                    .append("        },\n")
                    .append("        container: {\n")
                    .append(String.format("            type: \"%s\",\n",
                            containerTypeCombo.getValue() != null ? containerTypeCombo.getValue() : ""))
                    .append(String.format("            count: %d\n",
                            containerCountSpinner.getValue()))
                    .append("        },\n")
                    .append(String.format("        irrigation_level: %.0f\n",
                            approximateVolSlider.getValue()))
                    .append("    }\n")
                    .append("});\n");

            saveToFile(nosql.toString(), "NoSQL Script", "js");

        } catch (Exception e) {
            showAlert("Error", "Error al generar script NoSQL: " + e.getMessage());
        }
    }

    private void exportToJSON() {
        try {
            StringBuilder json = new StringBuilder();

            // Crear objeto JSON
            json.append("{\n")
                    .append("    \"agricultural_record\": {\n")
                    .append(String.format("        \"timestamp\": \"%s\",\n",
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .append(String.format("        \"crop_type\": \"%s\",\n",
                            cropTypeCombo.getValue()))
                    .append("        \"sensor_data\": {\n")
                    .append("            \"soil_humidity\": 0.00,\n")    // Del CSV
                    .append("            \"air_temperature\": 0.00,\n")  // Del CSV
                    .append("            \"air_humidity\": 0.00\n")      // Del CSV
                    .append("        },\n")
                    .append("        \"irrigation_data\": {\n")
                    .append("            \"water\": {\n")
                    .append(String.format("                \"temperature\": %.2f,\n",
                            Double.parseDouble(tempInput.getText())))
                    .append(String.format("                \"temperature_unit\": \"%s\",\n",
                            tempUnitCombo.getValue()))
                    .append(String.format("                \"volume\": %.2f,\n",
                            Double.parseDouble(volInput.getText())))
                    .append(String.format("                \"volume_unit\": \"%s\"\n",
                            volUnitCombo.getValue()))
                    .append("            },\n")
                    .append("            \"container\": {\n")
                    .append(String.format("                \"type\": \"%s\",\n",
                            containerTypeCombo.getValue() != null ? containerTypeCombo.getValue() : ""))
                    .append(String.format("                \"count\": %d\n",
                            containerCountSpinner.getValue()))
                    .append("            },\n")
                    .append(String.format("            \"irrigation_level\": %.0f\n",
                            approximateVolSlider.getValue()))
                    .append("        }\n")
                    .append("    }\n")
                    .append("}\n");

            saveToFile(json.toString(), "JSON Data", "json");

        } catch (Exception e) {
            showAlert("Error", "Error al generar JSON: " + e.getMessage());
        }
    }

    private void saveToFile(String content, String description, String extension) {
        if (!validateExport()) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar " + description);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(description, "*." + extension)
        );

        File file = fileChooser.showSaveDialog(mainContainer.getScene().getWindow());
        if (file != null) {
            try {
                Files.writeString(file.toPath(), content);
                showAlert("Éxito", "Archivo generado correctamente");
            } catch (IOException e) {
                showAlert("Error", "Error al guardar el archivo: " + e.getMessage());
            }
        }
    }

    private boolean validateExport() {
        if (cropTypeCombo.getValue() == null || cropTypeCombo.getValue().isEmpty()) {
            showAlert("Error", "Seleccione un tipo de cultivo");
            return false;
        }

        try {
            if (tempInput.getText().isEmpty()) {
                showAlert("Error", "Ingrese la temperatura del agua");
                return false;
            }
            Double.parseDouble(tempInput.getText());

            if (volInput.getText().isEmpty()) {
                showAlert("Error", "Ingrese el volumen de agua");
                return false;
            }
            Double.parseDouble(volInput.getText());

        } catch (NumberFormatException e) {
            showAlert("Error", "Los valores numéricos son inválidos");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(
                title.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    public void stop() {
        if(clockTimeline != null) {
            clockTimeline.stop();
        }
    }
}
