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

import java.util.HashMap;
import java.util.Map;

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
        try {
            approximateVolSlider.setValue(latestData.irrigationLevel());
            dateTimeHandler.updateLastUpdateTime(latestData.timestamp());
            if (latestData.cropType() != null && cropTypeCombo.getItems().contains(latestData.cropType())) {
                cropTypeCombo.setValue(latestData.cropType());
            }
            updateSystemStatus();
            System.out.println("Dashboard inicializado con datos del sensor");
        } catch (Exception e) {
            System.err.println("Error al inicializar dashboard con datos: " + e.getMessage());
            showAlert("Error", "Error al cargar los datos: " + e.getMessage());
        }
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
            temperatureHandler = new TemperatureHandler(tempInput, tempUnitCombo);
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

            validationHandler = new ValidationHandler(temperatureHandler, measurementHandler);

            exportHandler = new ExportHandler();

            dateTimeHandler = new DateTimeHandler(dateLabel, lastUpdateLabel);

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
        SensorDataEnriched latestSensorData = DataTransformationService.getInstance().getLatestReading();
        if (latestSensorData == null) {
            System.err.println("No hay datos de sensor disponibles");
            return null;
        }
        var measurement = measurementHandler.getCurrentMeasurement();
        Map<String, Object> measurementDetails = new HashMap<>();
        switch (measurement.type()) {
            case CONTAINER -> {
                measurementDetails.put("containerType", containerTypeCombo.getValue());
                measurementDetails.put("containerCount", containerCountSpinner.getValue());
            }
            case PUMP -> {
                measurementDetails.put("pumpType", pumpTypeCombo.getValue());
                measurementDetails.put("pumpTime", pumpTimeInput.getText());
            }
            case HOSE -> {
                measurementDetails.put("hoseType", hoseTypeCombo.getValue());
                measurementDetails.put("hoseTime", hoseTimeInput.getText());
            }
            case FURROW -> {
                measurementDetails.put("length", furrowLengthInput.getText());
                measurementDetails.put("width", furrowWidthInput.getText());
                measurementDetails.put("depth", furrowDepthCombo.getValue());
            }
        }

        return new ExportData(
                cropTypeCombo.getValue(),                    // cropType
                temperatureHandler.getCurrentTemperature(),   // waterTemperature
                temperatureHandler.getCurrentUnit(),          // temperatureUnit
                measurement.volume(),                         // waterVolume
                measurement.unit().getSymbol(),              // volumeUnit
                measurement.type().getValue(),                // measurementMethod
                measurementDetails,                           // measurementDetails como Map
                approximateVolSlider.getValue(),              // irrigationLevel
                latestSensorData.soilHumidity(),             // soilHumidity
                latestSensorData.airTemperature(),           // airTemperature
                latestSensorData.airHumidity()               // airHumidity
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

    private boolean validateExport() {
        ValidationResult result = validationHandler.validateExportData(cropTypeCombo.getValue());

        if (!result.isValid()) {
            showAlert("Error", result.getMessage());
            return false;
        }

        return true;
    }
}



