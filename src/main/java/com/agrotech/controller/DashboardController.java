package com.agrotech.controller;

import com.agrotech.handler.*;
import com.agrotech.model.ExportData;
import com.agrotech.model.SensorDataEnriched;
import com.agrotech.model.ValidationResult;
import com.agrotech.service.DataTransformationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

/**
 * Controlador principal del dashboard para el sistema de riego agrícola.
 * Maneja la visualización y registro de datos de riego, incluyendo temperatura
 * y volumen del agua, así como la exportación de datos en diferentes formatos.
 */
public class DashboardController {

    @FXML private BorderPane mainContainer;

    @FXML private Label dateLabel;
    @FXML private ComboBox<String> cropTypeCombo;
    @FXML private Circle statusIndicator;
    @FXML private Label systemStatusLabel;
    @FXML private Label lastUpdateLabel;


    @FXML private TextField tempInput;
    @FXML private ComboBox<String> tempUnitCombo;

    @FXML private TextField volInput;
    @FXML private ComboBox<String> volUnitCombo;

    @FXML private RadioButton containerRadio;
    @FXML private ComboBox<String> containerTypeCombo;
    @FXML private Spinner<Integer> containerCountSpinner;

    @FXML private RadioButton pumpRadio;
    @FXML private ComboBox<String> pumpTypeCombo;
    @FXML private TextField pumpTimeInput;

    @FXML private RadioButton hoseRadio;
    @FXML private ComboBox<String> hoseTypeCombo;
    @FXML private TextField hoseTimeInput;

    @FXML private RadioButton furrowRadio;
    @FXML private TextField furrowLengthInput;
    @FXML private TextField furrowWidthInput;
    @FXML private ComboBox<String> furrowDepthCombo;



    @FXML private Slider approximateVolSlider;
    @FXML private Label sliderLabel;

    @FXML private Button sqlButton;
    @FXML private Button nosqlButton;
    @FXML private Button jsonButton;

    private TemperatureHandler temperatureHandler;
    private ExportHandler exportHandler;
    private DateTimeHandler dateTimeHandler;
    private MeasurementHandler measurementHandler;
    private ValidationHandler validationHandler;


    @FXML
    public void initialize() {
        // Inicializar handlers
        initializeHandlers();

        // Configurar paneles
        setupTopPanel();
        setupMainPanel();
        setupBottomPanel();

        // Configurar tooltips
        setupTooltips();

        // Iniciar actualizaciones de tiempo
        dateTimeHandler.startClock();
    }

    public void initializeWithData() {
        SensorDataEnriched latestData = DataTransformationService.getInstance().getLatestReading();
        if (latestData == null) {
            showAlert("Error", "No hay datos disponibles para mostrar");
            return;
        }

        // Inicializar campos con los últimos datos
        tempInput.setText(String.format("%.1f", latestData.waterData().temperature()));
        tempUnitCombo.setValue(latestData.waterData().temperatureUnit());

        // Actualizar última actualización
        dateTimeHandler.updateLastUpdateTime(latestData.timestamp());

        // Configurar el slider con el nivel de irrigación
        approximateVolSlider.setValue(latestData.irrigationLevel());

        // Actualizar estado del sistema
        updateSystemStatus();
    }

    private void setupTooltips() {
        // Tooltips para campos de temperatura
        tempInput.setTooltip(new Tooltip("Ingrese la temperatura del agua"));
        tempUnitCombo.setTooltip(new Tooltip("Seleccione la unidad de temperatura"));

        // Tooltips para campos de volumen
        volInput.setTooltip(new Tooltip("Ingrese el volumen de agua"));
        volUnitCombo.setTooltip(new Tooltip("Seleccione la unidad de volumen"));

        // Tooltips para botones de exportación
        sqlButton.setTooltip(new Tooltip("Exportar datos en formato SQL"));
        nosqlButton.setTooltip(new Tooltip("Exportar datos en formato NoSQL"));
        jsonButton.setTooltip(new Tooltip("Exportar datos en formato JSON"));

        // Tooltips para medidas
        containerRadio.setTooltip(new Tooltip("Medir por contenedores"));
        containerTypeCombo.setTooltip(new Tooltip("Seleccione el tipo de contenedor"));
        containerCountSpinner.setTooltip(new Tooltip("Cantidad de contenedores"));
    }

    private void initializeHandlers() {
        try {
            // 1. Temperatura
            temperatureHandler = new TemperatureHandler(tempInput, tempUnitCombo);

            // 2. Measurement - debe ir ANTES del ValidationHandler
            measurementHandler = new MeasurementHandler(
                    volInput,
                    volUnitCombo,
                    containerRadio,
                    containerTypeCombo,
                    containerCountSpinner,
                    pumpRadio,
                    pumpTypeCombo,
                    pumpTimeInput,
                    hoseRadio,
                    hoseTypeCombo,
                    hoseTimeInput,
                    furrowRadio,
                    furrowLengthInput,
                    furrowWidthInput,
                    furrowDepthCombo
            );

            // 3. Validation - debe ir DESPUÉS de crear los otros handlers
            validationHandler = new ValidationHandler(temperatureHandler, measurementHandler);

            // 4. Export
            exportHandler = new ExportHandler();

            // 5. DateTime
            dateTimeHandler = new DateTimeHandler(dateLabel, lastUpdateLabel);

            // Configurar callback para actualización de volumen
            measurementHandler.setOnVolumeUpdated(data -> {
                dateTimeHandler.updateLastUpdateTime();
                updateSliderLabel(approximateVolSlider.getValue());
            });
        } catch (Exception e) {
            String errorMessage = "Error al inicializar handlers: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            showAlert("Error", errorMessage);
        }
    }

    private void setupTopPanel() {
        setupCropInfo();
        updateSystemStatus();
        dateTimeHandler.updateLastUpdateTime();

        // Tooltips
        cropTypeCombo.setTooltip(new Tooltip("Seleccione el tipo de cultivo"));
        Tooltip.install(statusIndicator, new Tooltip("Indicador del estado del sistema"));
    }

    private void setupMainPanel() {
        setupVolumeSlider();
        setupTooltips();
    }

    private void setupBottomPanel() {
        setupExportButtons();
    }

    private void setupCropInfo() {
        cropTypeCombo.getItems().addAll("Oregano", "Albahaca", "Menta", "Romero");
        cropTypeCombo.setPromptText("Seleccionar cultivo");
        cropTypeCombo.setOnAction(e -> onCropTypeChanged());
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


    private void setupExportButtons() {
        sqlButton.setOnAction(e -> {
            if (validateExport()) {
                ExportData data = createExportData();
                exportHandler.exportToSQL(mainContainer.getScene().getWindow(), data);
            }
        });

        nosqlButton.setOnAction(e -> {
            if (validateExport()) {
                ExportData data = createExportData();
                exportHandler.exportToNoSQL(mainContainer.getScene().getWindow(), data);
            }
        });

        jsonButton.setOnAction(e -> {
            if (validateExport()) {
                ExportData data = createExportData();
                exportHandler.exportToJSON(mainContainer.getScene().getWindow(), data);
            }
        });
    }

    private ExportData createExportData() {
        var measurement = measurementHandler.getCurrentMeasurement();
        return new ExportData(
                cropTypeCombo.getValue(),
                temperatureHandler.getCurrentTemperature(),
                temperatureHandler.getCurrentUnit(),
                measurement.volume(),
                measurement.unit().getSymbol(),
                measurement.type().getValue(),
                measurementHandler.getMeasurementDetailsJson(),
                approximateVolSlider.getValue()
        );
    }

    private void updateSystemStatus() {
        statusIndicator.getStyleClass().removeAll("warning", "error");
        statusIndicator.getStyleClass().add("success");
        systemStatusLabel.setText("Sistema Operativo");
    }

    private void updateSliderLabel(double value) {
        String description = value < 33 ? "Riego Ligero" :
                value < 66 ? "Riego Moderado" :
                        "Riego Abundante";
        sliderLabel.setText(description + String.format(" (%.0f%%)", value));
    }

    private void onCropTypeChanged() {
        if (cropTypeCombo.getValue() != null) {
            updateSystemStatus();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void setTempFria() {
        temperatureHandler.setPresetTemperature("fria");
    }

    @FXML
    private void setTempTemplada() {
        temperatureHandler.setPresetTemperature("templada");
    }

    @FXML
    private void setTempCaliente() {
        temperatureHandler.setPresetTemperature("caliente");
    }

    @FXML
    public void stop() {
        dateTimeHandler.stopClock();
    }

    private boolean validateExport() {
        ValidationResult result = validationHandler.validateExportData(cropTypeCombo.getValue());

        if (!result.isValid()) {
            showAlert("Error", result.getMessage());
            return false;
        }

        return true;
    }

    private void handleExport(String type) {
        if (!validateExport()) {
            return;
        }

        ExportData data = createExportData();
        switch (type) {
            case "SQL" -> exportHandler.exportToSQL(mainContainer.getScene().getWindow(), data);
            case "NoSQL" -> exportHandler.exportToNoSQL(mainContainer.getScene().getWindow(), data);
            case "JSON" -> exportHandler.exportToJSON(mainContainer.getScene().getWindow(), data);
        }
    }
}



