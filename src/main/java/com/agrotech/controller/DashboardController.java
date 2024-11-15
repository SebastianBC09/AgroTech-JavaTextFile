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
import java.util.Map;

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
    @FXML private ToggleGroup measureType;
    @FXML private RadioButton containerRadio;
    @FXML private RadioButton pumpRadio;
    @FXML private RadioButton hoseRadio;
    @FXML private RadioButton furrowRadio;
    @FXML private ComboBox<String> pumpTypeCombo;
    @FXML private TextField pumpTimeInput;
    @FXML private ComboBox<String> hoseTypeCombo;
    @FXML private TextField hoseTimeInput;
    @FXML private TextField furrowLengthInput;
    @FXML private TextField furrowWidthInput;
    @FXML private ComboBox<String> furrowDepthCombo;
    @FXML private ComboBox<String> comparisonCombo;
    @FXML private ComboBox<String> comparisonFactorCombo;

    private Timeline clockTimeline;

    private static final Map<String, Double> PUMP_FLOW_RATES = Map.of(
            "Bomba 1HP (3600 L/h)", 3600.0,
            "Bomba 2HP (7200 L/h)", 7200.0,
            "Motobomba (10000 L/h)", 10000.0
    );

    private static final Map<String, Double> HOSE_FLOW_RATES = Map.of(
            "Manguera 1/2\" (500 L/h)", 500.0,
            "Manguera 3/4\" (1000 L/h)", 1000.0,
            "Manguera 1\" (2000 L/h)", 2000.0
    );

    private static final Map<String, Double> CONTAINER_VOLUMES = Map.of(
            "Balde (20L)", 20.0,
            "Tanque (200L)", 200.0,
            "Bidón (5L)", 5.0
    );

    private static final Map<String, Double> FURROW_DEPTHS = Map.of(
            "Bajo (5cm)", 0.05,
            "Medio (10cm)", 0.10,
            "Alto (15cm)", 0.15
    );

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

    private void setupVolumeControls() {
        setupBasicMeasures();
        setupContainerControls();
        setupFlowBasedControls();
        setupFurrowControls();
        setupComparisonControls();
        setupVolumeSlider();
        setupVolumeCalculationListeners();
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

    private void setupBasicMeasures() {
        // Unidades básicas
        volUnitCombo.getItems().addAll("L", "mL", "m³");
        volUnitCombo.setValue("L");
        volUnitCombo.setOnAction(e -> convertVolume());

        // Validación numérica para entrada de volumen
        volInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                volInput.setText(old);
            }
        });
    }

    private void setupContainerControls() {
        containerTypeCombo.getItems().addAll(CONTAINER_VOLUMES.keySet());
        containerCountSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1)
        );
    }

    private void setupFlowBasedControls() {
        // Configuración de bombas
        pumpTypeCombo.getItems().addAll(PUMP_FLOW_RATES.keySet());
        setupNumericInput(pumpTimeInput);

        // Configuración de mangueras
        hoseTypeCombo.getItems().addAll(HOSE_FLOW_RATES.keySet());
        setupNumericInput(hoseTimeInput);
    }

    private void setupFurrowControls() {
        furrowDepthCombo.getItems().addAll(FURROW_DEPTHS.keySet());
        setupNumericInput(furrowLengthInput);
        setupNumericInput(furrowWidthInput);
    }

    private void setupComparisonControls() {
        comparisonCombo.getItems().addAll(
                "Igual que ayer",
                "Igual que la semana pasada",
                "Igual que hace 15 días"
        );

        comparisonFactorCombo.getItems().addAll(
                "La mitad",
                "El doble",
                "Un tercio",
                "El triple"
        );

        // Listener para comparaciones
        comparisonCombo.setOnAction(e -> updateVolumeFromComparison());
        comparisonFactorCombo.setOnAction(e -> updateVolumeFromComparison());
    }

    private void setupNumericInput(TextField input) {
        input.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                input.setText(old);
            }
        });
    }

    private void setupVolumeCalculationListeners() {
        measureType.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                updateVolumeInputs((RadioButton) newVal);
            }
        });

        // Listeners específicos para cada tipo de medida
        setupPumpListeners();
        setupHoseListeners();
        setupFurrowListeners();
        setupContainerListeners();
    }

    private void setupPumpListeners() {
        pumpRadio.selectedProperty().addListener((obs, old, newVal) -> {
            pumpTypeCombo.setDisable(!newVal);
            pumpTimeInput.setDisable(!newVal);
            if (newVal) calculatePumpVolume();
        });

        pumpTypeCombo.setOnAction(e -> {
            if (pumpRadio.isSelected()) calculatePumpVolume();
        });

        pumpTimeInput.textProperty().addListener((obs, old, newVal) -> {
            if (pumpRadio.isSelected()) calculatePumpVolume();
        });
    }

    private void setupHoseListeners() {
        hoseRadio.selectedProperty().addListener((obs, old, newVal) -> {
            hoseTypeCombo.setDisable(!newVal);
            hoseTimeInput.setDisable(!newVal);
            if (newVal) calculateHoseVolume();
        });

        hoseTypeCombo.setOnAction(e -> {
            if (hoseRadio.isSelected()) calculateHoseVolume();
        });

        hoseTimeInput.textProperty().addListener((obs, old, newVal) -> {
            if (hoseRadio.isSelected()) calculateHoseVolume();
        });
    }

    private void setupFurrowListeners() {
        furrowRadio.selectedProperty().addListener((obs, old, newVal) -> {
            furrowLengthInput.setDisable(!newVal);
            furrowWidthInput.setDisable(!newVal);
            furrowDepthCombo.setDisable(!newVal);
            if (newVal) calculateFurrowVolume();
        });

        furrowDepthCombo.setOnAction(e -> {
            if (furrowRadio.isSelected()) calculateFurrowVolume();
        });
    }

    private void setupContainerListeners() {
        containerRadio.selectedProperty().addListener((obs, old, newVal) -> {
            containerTypeCombo.setDisable(!newVal);
            containerCountSpinner.setDisable(!newVal);
            if (newVal) calculateContainerVolume();
        });

        containerTypeCombo.setOnAction(e -> {
            if (containerRadio.isSelected()) calculateContainerVolume();
        });

        containerCountSpinner.valueProperty().addListener((obs, old, newVal) -> {
            if (containerRadio.isSelected()) calculateContainerVolume();
        });
    }

    private void calculatePumpVolume() {
        try {
            String pumpType = pumpTypeCombo.getValue();
            double minutes = Double.parseDouble(pumpTimeInput.getText());
            double flowRate = PUMP_FLOW_RATES.getOrDefault(pumpType, 0.0);
            double volume = (flowRate / 60) * minutes;
            updateVolume(volume, "L");
        } catch (NumberFormatException ignored) {}
    }

    private void calculateHoseVolume() {
        try {
            String hoseType = hoseTypeCombo.getValue();
            double minutes = Double.parseDouble(hoseTimeInput.getText());
            double flowRate = HOSE_FLOW_RATES.getOrDefault(hoseType, 0.0);
            double volume = (flowRate / 60) * minutes;
            updateVolume(volume, "L");
        } catch (NumberFormatException ignored) {}
    }

    private void calculateFurrowVolume() {
        try {
            double length = Double.parseDouble(furrowLengthInput.getText());
            double width = Double.parseDouble(furrowWidthInput.getText());
            String depthType = furrowDepthCombo.getValue();
            double depth = FURROW_DEPTHS.getOrDefault(depthType, 0.0);

            double volumeM3 = length * width * depth;
            double volumeLiters = volumeM3 * 1000; // Convertir m³ a litros
            updateVolume(volumeLiters, "L");
        } catch (NumberFormatException ignored) {}
    }

    private void calculateContainerVolume() {
        String containerType = containerTypeCombo.getValue();
        if (containerType == null) return;

        double baseVolume = CONTAINER_VOLUMES.getOrDefault(containerType, 0.0);
        double totalVolume = baseVolume * containerCountSpinner.getValue();
        updateVolume(totalVolume, "L");
    }

    private void updateVolume(double volume, String unit) {
        volInput.setText(String.format("%.2f", volume));
        volUnitCombo.setValue(unit);
    }

    private void convertVolume() {
        if (volInput.getText().isEmpty()) return;

        try {
            double volume = Double.parseDouble(volInput.getText());
            String targetUnit = volUnitCombo.getValue();

            // Primero convertimos el valor actual a litros (unidad base)
            double volumeInLiters = switch (targetUnit) {
                case "mL" -> volume / 1000;    // mL a L
                case "m³" -> volume * 1000;    // m³ a L (1 m³ = 1000 L)
                default -> volume;             // ya está en L
            };

            // Luego convertimos de litros a la unidad seleccionada
            double convertedVolume = switch (targetUnit) {
                case "mL" -> volumeInLiters * 1000;  // L a mL
                case "m³" -> volumeInLiters / 1000;  // L a m³
                default -> volumeInLiters;           // mantener en L
            };

            volInput.setText(String.format("%.2f", convertedVolume));

        } catch (NumberFormatException e) {
            // Si hay error de conversión, mantener el valor anterior
            volInput.setText("0.00");
        }
    }

    private void updateVolumeFromComparison() {
        String comparisonBase = comparisonCombo.getValue();
        String factor = comparisonFactorCombo.getValue();

        if (comparisonBase == null || factor == null) return;

        // Aquí normalmente obtendrías el valor histórico de una base de datos
        // Por ahora usaremos valores de ejemplo
        double baseVolume = switch (comparisonBase) {
            case "Igual que ayer" -> 100.0;          // Ejemplo: 100L ayer
            case "Igual que la semana pasada" -> 90.0;  // Ejemplo: 90L semana pasada
            case "Igual que hace 15 días" -> 80.0;      // Ejemplo: 80L hace 15 días
            default -> 0.0;
        };

        // Aplicar el factor de multiplicación
        double adjustedVolume = switch (factor) {
            case "La mitad" -> baseVolume * 0.5;
            case "El doble" -> baseVolume * 2.0;
            case "Un tercio" -> baseVolume / 3.0;
            case "El triple" -> baseVolume * 3.0;
            default -> baseVolume;
        };

        // Actualizar el campo de volumen y mostrar un mensaje informativo
        updateVolume(adjustedVolume, "L");

        // Mostrar mensaje informativo
        String message = String.format(
                "Volumen ajustado a %.2f L (%s, %s del registro de %s)",
                adjustedVolume,
                factor.toLowerCase(),
                comparisonBase.toLowerCase()
        );

        showVolumeUpdateInfo(message);
    }

    private void showVolumeUpdateInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Actualización de Volumen");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateVolumeInputs(RadioButton selectedButton) {
        if (selectedButton == null) return;

        // Deshabilitar todos los controles primero
        disableAllVolumeControls(true);

        // Habilitar solo los controles correspondientes al tipo seleccionado
        if (selectedButton == containerRadio) {
            containerTypeCombo.setDisable(false);
            containerCountSpinner.setDisable(false);
            calculateContainerVolume();
        }
        else if (selectedButton == pumpRadio) {
            pumpTypeCombo.setDisable(false);
            pumpTimeInput.setDisable(false);
            calculatePumpVolume();
        }
        else if (selectedButton == hoseRadio) {
            hoseTypeCombo.setDisable(false);
            hoseTimeInput.setDisable(false);
            calculateHoseVolume();
        }
        else if (selectedButton == furrowRadio) {
            furrowLengthInput.setDisable(false);
            furrowWidthInput.setDisable(false);
            furrowDepthCombo.setDisable(false);
            calculateFurrowVolume();
        }
    }

    private void disableAllVolumeControls(boolean disable) {
        // Controles de contenedor
        containerTypeCombo.setDisable(disable);
        containerCountSpinner.setDisable(disable);

        // Controles de bomba
        pumpTypeCombo.setDisable(disable);
        pumpTimeInput.setDisable(disable);

        // Controles de manguera
        hoseTypeCombo.setDisable(disable);
        hoseTimeInput.setDisable(disable);

        // Controles de surco
        furrowLengthInput.setDisable(disable);
        furrowWidthInput.setDisable(disable);
        furrowDepthCombo.setDisable(disable);
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
