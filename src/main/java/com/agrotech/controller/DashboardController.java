package com.agrotech.controller;

import com.agrotech.handler.*;
import com.agrotech.model.ExportData;
import com.agrotech.model.ValidationResult;
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

        // Iniciar actualizaciones de tiempo
        dateTimeHandler.startClock();
    }

    private void initializeHandlers() {
        temperatureHandler = new TemperatureHandler(tempInput, tempUnitCombo);
        exportHandler = new ExportHandler();
        dateTimeHandler = new DateTimeHandler(dateLabel, lastUpdateLabel);
        validationHandler = new ValidationHandler(temperatureHandler, measurementHandler);

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

        // Configurar callback para actualización de volumen
        measurementHandler.setOnVolumeUpdated(data -> {
            dateTimeHandler.updateLastUpdateTime();
            updateSliderLabel(approximateVolSlider.getValue());
        });
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

    private void setupTooltips() {
        volInput.setTooltip(new Tooltip("Volumen de agua"));
        volUnitCombo.setTooltip(new Tooltip("Unidad de volumen"));
        approximateVolSlider.setTooltip(new Tooltip("Nivel aproximado de riego"));
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
        Alert alert = new Alert(
                title.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION
        );
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



