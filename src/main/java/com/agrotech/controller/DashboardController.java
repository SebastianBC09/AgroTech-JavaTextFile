package com.agrotech.controller;

import com.agrotech.handler.ExportHandler;
import com.agrotech.handler.TemperatureHandler;
import com.agrotech.model.ExportData;
import com.agrotech.service.TemperatureService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.animation.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Controlador principal del dashboard para el sistema de riego agrícola.
 * Maneja la visualización y registro de datos de riego, incluyendo temperatura
 * y volumen del agua, así como la exportación de datos en diferentes formatos.
 */
public class DashboardController {
    // region Constantes
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final TemperatureService temperatureService;

    public DashboardController() {
        this.temperatureService = new TemperatureService();
    }

    /**
     * Tasas de flujo para diferentes tipos de bombas (L/h)
     */
    private static final Map<String, Double> PUMP_FLOW_RATES = Map.of(
            "Bomba 1HP (3600 L/h)", 3600.0,
            "Bomba 2HP (7200 L/h)", 7200.0,
            "Motobomba (10000 L/h)", 10000.0
    );

    /**
     * Tasas de flujo para diferentes tipos de mangueras (L/h)
     */
    private static final Map<String, Double> HOSE_FLOW_RATES = Map.of(
            "Manguera 1/2\" (500 L/h)", 500.0,
            "Manguera 3/4\" (1000 L/h)", 1000.0,
            "Manguera 1\" (2000 L/h)", 2000.0
    );

    /**
     * Volúmenes predefinidos para diferentes tipos de contenedores (L)
     */
    private static final Map<String, Double> CONTAINER_VOLUMES = Map.of(
            "Balde (20L)", 20.0,
            "Tanque (200L)", 200.0,
            "Bidón (5L)", 5.0
    );

    /**
     * Profundidades predefinidas para surcos (m)
     */
    private static final Map<String, Double> FURROW_DEPTHS = Map.of(
            "Bajo (5cm)", 0.05,
            "Medio (10cm)", 0.10,
            "Alto (15cm)", 0.15
    );
    // endregion

    // region FXML Injections
    @FXML private BorderPane mainContainer;

    // Panel Superior
    @FXML private Label dateLabel;
    @FXML private ComboBox<String> cropTypeCombo;
    @FXML private Circle statusIndicator;
    @FXML private Label systemStatusLabel;
    @FXML private Label lastUpdateLabel;

    // Controles de Temperatura
    @FXML private TextField tempInput;
    @FXML private ComboBox<String> tempUnitCombo;

    private TemperatureHandler temperatureHandler;
    private ExportHandler exportHandler;

    // Controles de Volumen Básicos
    @FXML private TextField volInput;
    @FXML private ComboBox<String> volUnitCombo;

    // Controles de Medida por Contenedor
    @FXML private ToggleGroup measureType;
    @FXML private RadioButton containerRadio;
    @FXML private ComboBox<String> containerTypeCombo;
    @FXML private Spinner<Integer> containerCountSpinner;

    // Controles de Medida por Bomba
    @FXML private RadioButton pumpRadio;
    @FXML private ComboBox<String> pumpTypeCombo;
    @FXML private TextField pumpTimeInput;

    // Controles de Medida por Manguera
    @FXML private RadioButton hoseRadio;
    @FXML private ComboBox<String> hoseTypeCombo;
    @FXML private TextField hoseTimeInput;

    // Controles de Medida por Surco
    @FXML private RadioButton furrowRadio;
    @FXML private TextField furrowLengthInput;
    @FXML private TextField furrowWidthInput;
    @FXML private ComboBox<String> furrowDepthCombo;

    // Controles de Comparación
    @FXML private ComboBox<String> comparisonCombo;
    @FXML private ComboBox<String> comparisonFactorCombo;

    // Controles de Nivel de Riego
    @FXML private Slider approximateVolSlider;
    @FXML private Label sliderLabel;

    // Botones de Exportación
    @FXML private Button sqlButton;
    @FXML private Button nosqlButton;
    @FXML private Button jsonButton;
    // endregion

    // Variables de clase
    private Timeline clockTimeline;

    // region Métodos de Inicialización
    @FXML
    public void initialize() {
        temperatureHandler = new TemperatureHandler(tempInput, tempUnitCombo);
        exportHandler = new ExportHandler();
        setupDateTime();
        setupTopPanel();
        setupCenterPanel();
        setupMainPanel();
        setupBottomPanel();

        // Habilitar controles cuando se seleccione un método de medición
        measureType.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                updateVolumeInputs((RadioButton) newVal);
            }
        });

        // Habilitar los botones de radio por defecto
        containerRadio.setDisable(false);
        pumpRadio.setDisable(false);
        hoseRadio.setDisable(false);
        furrowRadio.setDisable(false);
    }

    /**
     * Configura el reloj y actualizaciones de fecha/hora.
     */
    private void setupDateTime() {
        clockTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateDateTime())
        );
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    /**
     * Configura el panel superior con información general.
     */
    private void setupTopPanel() {
        updateDateTime();
        setupCropInfo();
        updateSystemStatus();
        updateLastUpdateTime();

        // Tooltips
        cropTypeCombo.setTooltip(new Tooltip("Seleccione el tipo de cultivo"));
        // Para el Circle, usamos Tooltip.install en lugar de setTooltip
        Tooltip statusTooltip = new Tooltip("Indicador del estado del sistema");
        Tooltip.install(statusIndicator, statusTooltip);
    }

    /**
     * Configura el panel central con controles de temperatura y volumen.
     */
    private void setupCenterPanel() {
        setupVolumeControls();
    }

    /**
     * Configura el panel principal con tooltips y listeners.
     */
    private void setupMainPanel() {
        setupMainTooltips();
        setupMainListeners();
    }

    /**
     * Configura el panel inferior con botones de exportación.
     */
    private void setupBottomPanel() {
        setupExportButtons();
    }

// endregion

// region Métodos Auxiliares Generales
    /**
     * Configura los tooltips principales del dashboard.
     */
    private void setupMainTooltips() {
        volInput.setTooltip(new Tooltip("Volumen de agua"));
        volUnitCombo.setTooltip(new Tooltip("Unidad de volumen"));
        approximateVolSlider.setTooltip(new Tooltip("Nivel aproximado de riego"));
    }

    /**
     * Configura los listeners principales del dashboard.
     */
    private void setupMainListeners() {
        containerTypeCombo.setOnAction(e -> updateContainerVolume());
        containerCountSpinner.valueProperty().addListener((obs, old, newVal) ->
                updateContainerVolume());
        volUnitCombo.setOnAction(e -> convertVolume());
    }

    /**
     * Configura los botones de exportación.
     */
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
        return new ExportData(
                cropTypeCombo.getValue(),
                temperatureHandler.getCurrentTemperature(),
                temperatureHandler.getCurrentUnit(),
                Double.parseDouble(volInput.getText()),
                volUnitCombo.getValue(),
                getMeasurementMethod(),
                generateMeasurementDetailsJSON(),
                approximateVolSlider.getValue()
        );
    }

    /**
     * Actualiza la fecha y hora mostrada en el dashboard.
     */
    private void updateDateTime() {
        dateLabel.setText("Fecha: " +
                LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }

    /**
     * Configura la información del cultivo.
     */
    private void setupCropInfo() {
        cropTypeCombo.getItems().addAll("Oregano", "Albahaca", "Menta", "Romero");
        cropTypeCombo.setPromptText("Seleccionar cultivo");
        cropTypeCombo.setOnAction(e -> onCropTypeChanged());
    }

    /**
     * Actualiza el estado del sistema cuando cambia el cultivo seleccionado.
     */
    private void onCropTypeChanged() {
        if (cropTypeCombo.getValue() != null) {
            updateSystemStatus();
        }
    }

    /**
     * Actualiza los indicadores de estado del sistema.
     */
    private void updateSystemStatus() {
        statusIndicator.getStyleClass().removeAll("warning", "error");
        statusIndicator.getStyleClass().add("success");
        systemStatusLabel.setText("Sistema Operativo");
    }

    /**
     * Actualiza la hora de última actualización.
     */
    private void updateLastUpdateTime() {
        lastUpdateLabel.setText(
                LocalDateTime.now().format(TIME_FORMATTER)
        );
    }

    /**
     * Configura un campo de texto para aceptar solo números.
     * @param input Campo de texto a configurar
     */
    private void setupNumericInput(TextField input) {
        input.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("-?\\d*\\.?\\d*")) {
                input.setText(old);
            }
        });
    }

    /**
     * Muestra una alerta al usuario.
     * @param title Título de la alerta
     * @param content Contenido del mensaje
     */
    private void showAlert(String title, String content) {
        Alert.AlertType type = title.equals("Error") ?
                Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Método llamado por el framework JavaFX al cerrar la aplicación.
     * Limpia los recursos y detiene los timelines.
     */
    @FXML
    public void stop() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
    }
// endregion

// region Validaciones
    /**
     * Válida los datos antes de exportar.
     * @return true si los datos son válidos, false en caso contrario
     */
    private boolean validateExport() {
        if (cropTypeCombo.getValue() == null || cropTypeCombo.getValue().isEmpty()) {
            showAlert("Error", "Seleccione un tipo de cultivo");
            return false;
        }

        try {
            double temperature = temperatureHandler.getCurrentTemperature();
            if (temperature == 0.0) {
                showAlert("Error", "Ingrese la temperatura del agua");
                return false;
            }

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

// region Control de Volumen
    /**
     * Configura todos los controles relacionados con el volumen.
     */
    private void setupVolumeControls() {
        setupBasicMeasures();
        setupContainerControls();
        setupFlowBasedControls();
        setupFurrowControls();
        setupComparisonControls();
        setupVolumeSlider();
        setupVolumeCalculationListeners();
    }

    /**
     * Configura las medidas básicas de volumen.
     */
    private void setupBasicMeasures() {
        // Configurar unidades
        volUnitCombo.getItems().addAll("L", "mL", "m³");
        volUnitCombo.setValue("L");
        volUnitCombo.setPromptText("L");
        volUnitCombo.setOnAction(e -> convertVolume());

        // Validación numérica
        setupNumericInput(volInput);

        // Tooltips
        volInput.setTooltip(new Tooltip("Volumen de agua a utilizar"));
        volUnitCombo.setTooltip(new Tooltip("Unidad de medida del volumen"));
    }

    /**
     * Configura los controles de medición por contenedor.
     */
    private void setupContainerControls() {
        containerTypeCombo.getItems().addAll(CONTAINER_VOLUMES.keySet());
        containerCountSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1)
        );

        containerRadio.setTooltip(
                new Tooltip("Calcular volumen usando contenedores estándar")
        );
    }

    /**
     * Configura los controles basados en flujo (bombas y mangueras).
     */
    private void setupFlowBasedControls() {
        // Bombas
        pumpTypeCombo.getItems().addAll(PUMP_FLOW_RATES.keySet());
        setupNumericInput(pumpTimeInput);
        pumpRadio.setTooltip(
                new Tooltip("Calcular volumen según tiempo de bombeo")
        );

        // Mangueras
        hoseTypeCombo.getItems().addAll(HOSE_FLOW_RATES.keySet());
        setupNumericInput(hoseTimeInput);
        hoseRadio.setTooltip(
                new Tooltip("Calcular volumen según tiempo de riego con manguera")
        );
    }

    /**
     * Configura los controles de medición por surco.
     */
    private void setupFurrowControls() {
        furrowDepthCombo.getItems().addAll(FURROW_DEPTHS.keySet());
        setupNumericInput(furrowLengthInput);
        setupNumericInput(furrowWidthInput);
        furrowRadio.setTooltip(
                new Tooltip("Calcular volumen según dimensiones del surco")
        );
    }

    /**
     * Configura los controles de comparación con riegos anteriores.
     */
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

        comparisonCombo.setOnAction(e -> updateVolumeFromComparison());
        comparisonFactorCombo.setOnAction(e -> updateVolumeFromComparison());
    }

    /**
     * Configura el slider de nivel aproximado de riego.
     */
    private void setupVolumeSlider() {
        approximateVolSlider.setMin(0);
        approximateVolSlider.setMax(100);
        approximateVolSlider.setValue(50);
        approximateVolSlider.valueProperty().addListener(
                (obs, old, newVal) -> updateSliderLabel(newVal.doubleValue())
        );
        updateSliderLabel(50);
    }

    /**
     * Actualiza la etiqueta del slider según el valor seleccionado.
     */
    private void updateSliderLabel(double value) {
        String description = value < 33 ? "Riego Ligero" :
                value < 66 ? "Riego Moderado" :
                        "Riego Abundante";
        sliderLabel.setText(description + String.format(" (%.0f%%)", value));
    }

    /**
     * Configura los listeners para cálculos automáticos de volumen.
     */
    private void setupVolumeCalculationListeners() {
        // Listener para cambio de tipo de medida
        measureType.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                updateVolumeInputs((RadioButton) newVal);
            }
        });

        // Configurar los listeners específicos
        setupPumpListeners();
        setupHoseListeners();
        setupFurrowListeners();
        setupContainerListeners();

        // Inicialmente, deshabilitar todos los controles de medición
        disableAllVolumeControls();
    }

    /**
     * Configura los listeners específicos para cada tipo de medición.
     */
    private void setupMeasurementTypeListeners() {
        setupPumpListeners();
        setupHoseListeners();
        setupFurrowListeners();
        setupContainerListeners();
    }

    // region Cálculos y Actualizaciones de Volumen
    /**
     * Gestiona la visibilidad y estado de los controles según el tipo de medición.
     * @param selectedButton Botón de radio seleccionado
     */
    private void updateVolumeInputs(RadioButton selectedButton) {
        if (selectedButton == null) return;

        // Deshabilitar todos los controles de medición
        disableAllVolumeControls();

        // Habilitar solo los controles correspondientes al tipo seleccionado
        if (selectedButton == containerRadio) {
            enableContainerControls();
            calculateContainerVolume();
        }
        else if (selectedButton == pumpRadio) {
            enablePumpControls();
            calculatePumpVolume();
        }
        else if (selectedButton == hoseRadio) {
            enableHoseControls();
            calculateHoseVolume();
        }
        else if (selectedButton == furrowRadio) {
            enableFurrowControls();
            calculateFurrowVolume();
        }
    }

    /**
     * Habilita los controles específicos para medición por contenedor.
     */
    private void enableContainerControls() {
        containerTypeCombo.setDisable(false);
        containerCountSpinner.setDisable(false);
    }

    /**
     * Habilita los controles específicos para medición por bomba.
     */
    private void enablePumpControls() {
        pumpTypeCombo.setDisable(false);
        pumpTimeInput.setDisable(false);
    }

    /**
     * Habilita los controles específicos para medición por manguera.
     */
    private void enableHoseControls() {
        hoseTypeCombo.setDisable(false);
        hoseTimeInput.setDisable(false);
    }

    /**
     * Habilita los controles específicos para medición por surco.
     */
    private void enableFurrowControls() {
        furrowLengthInput.setDisable(false);
        furrowWidthInput.setDisable(false);
        furrowDepthCombo.setDisable(false);
    }

    /**
     * Calcula el volumen basado en contenedores seleccionados.
     */
    private void calculateContainerVolume() {
        String containerType = containerTypeCombo.getValue();
        if (containerType == null) return;

        double baseVolume = CONTAINER_VOLUMES.getOrDefault(containerType, 0.0);
        double totalVolume = baseVolume * containerCountSpinner.getValue();
        updateVolume(totalVolume);
    }

    /**
     * Calcula el volumen basado en el tiempo de uso de la bomba.
     */
    private void calculatePumpVolume() {
        try {
            String pumpType = pumpTypeCombo.getValue();
            if (pumpType == null || pumpTimeInput.getText().isEmpty()) return;

            double minutes = Double.parseDouble(pumpTimeInput.getText());
            double flowRate = PUMP_FLOW_RATES.getOrDefault(pumpType, 0.0);
            double volume = (flowRate / 60) * minutes; // Convertir de L/h a L/min
            updateVolume(volume);
        } catch (NumberFormatException ignored) {}
    }

    /**
     * Calcula el volumen basado en el tiempo de uso de la manguera.
     */
    private void calculateHoseVolume() {
        try {
            String hoseType = hoseTypeCombo.getValue();
            if (hoseType == null || hoseTimeInput.getText().isEmpty()) return;

            double minutes = Double.parseDouble(hoseTimeInput.getText());
            double flowRate = HOSE_FLOW_RATES.getOrDefault(hoseType, 0.0);
            double volume = (flowRate / 60) * minutes; // Convertir de L/h a L/min
            updateVolume(volume);
        } catch (NumberFormatException ignored) {}
    }

    /**
     * Calcula el volumen basado en las dimensiones del surco.
     */
    private void calculateFurrowVolume() {
        try {
            if (furrowLengthInput.getText().isEmpty() ||
                    furrowWidthInput.getText().isEmpty() ||
                    furrowDepthCombo.getValue() == null) return;

            double length = Double.parseDouble(furrowLengthInput.getText());
            double width = Double.parseDouble(furrowWidthInput.getText());
            double depth = FURROW_DEPTHS.getOrDefault(furrowDepthCombo.getValue(), 0.0);

            // Cálculo de volumen en metros cúbicos
            double volumeM3 = length * width * depth;
            // Conversión a litros (1 m³ = 1000 L)
            double volumeLiters = volumeM3 * 1000;

            updateVolume(volumeLiters);
        } catch (NumberFormatException ignored) {}
    }

    /**
     * Actualiza el volumen en la interfaz.
     * @param volume Volumen calculado
     */
    private void updateVolume(double volume) {
        volInput.setText(String.format("%.2f", volume));
        volUnitCombo.setValue("L");
        updateLastUpdateTime();
    }

    /**
     * Convierte el volumen entre diferentes unidades de medida.
     */
    private void convertVolume() {
        if (volInput.getText().isEmpty()) return;

        try {
            double volume = Double.parseDouble(volInput.getText());
            String currentUnit = volUnitCombo.getPromptText();
            String targetUnit = volUnitCombo.getValue();

            if (currentUnit.equals(targetUnit)) return;

            // Primero convertimos todo a litros (unidad base)
            double volumeInLiters = switch (currentUnit) {
                case "mL" -> volume / 1000;     // mL a L
                case "m³" -> volume * 1000;     // m³ a L (1 m³ = 1000 L)
                default -> volume;              // ya está en L
            };

            // Luego convertimos de litros a la unidad seleccionada
            double convertedVolume = switch (targetUnit) {
                case "mL" -> volumeInLiters * 1000;  // L a mL
                case "m³" -> volumeInLiters / 1000;  // L a m³
                default -> volumeInLiters;           // mantener en L
            };

            volInput.setText(String.format("%.3f", convertedVolume));
            volUnitCombo.setPromptText(targetUnit);

        } catch (NumberFormatException e) {
            showAlert("Error", "Valor de volumen inválido");
        }
    }

    private static double getConvertedVolume(String currentUnit, double volume, String targetUnit) {
        double volumeInLiters = switch (currentUnit) {
            case "mL" -> volume / 1000;     // mL a L
            case "m³" -> volume * 1000;     // m³ a L (1 m³ = 1000 L)
            default -> volume;              // ya está en L
        };

        // Luego convertimos de litros a la unidad seleccionada
        // L a mL
        // L a m³
        // mantener en L
        return switch (targetUnit) {
            case "mL" -> volumeInLiters * 1000;  // L a mL
            case "m³" -> volumeInLiters / 1000;  // L a m³
            default -> volumeInLiters;           // mantener en L
        };
    }

    /**
     * Actualiza el volumen basado en comparaciones con registros anteriores.
     */
    private void updateVolumeFromComparison() {
        String comparisonBase = comparisonCombo.getValue();
        String factor = comparisonFactorCombo.getValue();

        if (comparisonBase == null || factor == null) return;

        // TODO: Implementar obtención de datos históricos reales
        double baseVolume = getHistoricalVolume(comparisonBase);
        double adjustedVolume = applyComparisonFactor(baseVolume, factor);

        updateVolume(adjustedVolume);

        String message = String.format(
                "Volumen ajustado a %.2f L (%s, %s del registro de %s)",
                adjustedVolume,
                factor.toLowerCase(),
                comparisonBase.toLowerCase()
        );

        showVolumeUpdateInfo(message);
    }

    /**
     * Obtiene el volumen histórico según la base de comparación.
     * @param comparisonBase Base de comparación seleccionada
     * @return Volumen histórico
     */
    private double getHistoricalVolume(String comparisonBase) {
        // TODO: Implementar consulta a base de datos
        return switch (comparisonBase) {
            case "Igual que ayer" -> 100.0;
            case "Igual que la semana pasada" -> 90.0;
            case "Igual que hace 15 días" -> 80.0;
            default -> 0.0;
        };
    }

    /**
     * Aplica el factor de comparación al volumen base.
     * @param baseVolume Volumen base
     * @param factor Factor a aplicar
     * @return Volumen ajustado
     */
    private double applyComparisonFactor(double baseVolume, String factor) {
        return switch (factor) {
            case "La mitad" -> baseVolume * 0.5;
            case "El doble" -> baseVolume * 2.0;
            case "Un tercio" -> baseVolume / 3.0;
            case "El triple" -> baseVolume * 3.0;
            default -> baseVolume;
        };
    }

    /**
     * Muestra información sobre la actualización del volumen.
     * @param message Mensaje a mostrar
     */
    private void showVolumeUpdateInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Actualización de Volumen");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Deshabilita o habilita todos los controles de volumen.
     */
    private void disableAllVolumeControls() {
        // Controles de contenedor
        containerTypeCombo.setDisable(true);
        containerCountSpinner.setDisable(true);

        // Controles de bomba
        pumpTypeCombo.setDisable(true);
        pumpTimeInput.setDisable(true);

        // Controles de manguera
        hoseTypeCombo.setDisable(true);
        hoseTimeInput.setDisable(true);

        // Controles de surco
        furrowLengthInput.setDisable(true);
        furrowWidthInput.setDisable(true);
        furrowDepthCombo.setDisable(true);
    }

    /**
     * Actualiza el volumen basado en el contenedor seleccionado.
     */
    private void updateContainerVolume() {
        if (!containerRadio.isSelected() || containerTypeCombo.getValue() == null) return;

        double baseVolume = CONTAINER_VOLUMES.getOrDefault(
                containerTypeCombo.getValue(), 0.0);
        double totalVolume = baseVolume * containerCountSpinner.getValue();
        updateVolume(totalVolume);
    }

    /**
     * Configura los listeners para la medición por bomba.
     */
    private void setupPumpListeners() {
        pumpRadio.selectedProperty().addListener((obs, old, newVal) -> {
            pumpTypeCombo.setDisable(!newVal);
            pumpTimeInput.setDisable(!newVal);
            if (newVal) calculatePumpVolume();
        });

        pumpTimeInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                pumpTimeInput.setText(old);
            } else if (pumpRadio.isSelected()) {
                calculatePumpVolume();
            }
        });

        pumpTypeCombo.setOnAction(e -> {
            if (pumpRadio.isSelected()) calculatePumpVolume();
        });
    }

    /**
     * Configura los listeners para la medición por manguera.
     */
    private void setupHoseListeners() {
        hoseRadio.selectedProperty().addListener((obs, old, newVal) -> {
            hoseTypeCombo.setDisable(!newVal);
            hoseTimeInput.setDisable(!newVal);
            if (newVal) calculateHoseVolume();
        });

        hoseTimeInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                hoseTimeInput.setText(old);
            } else if (hoseRadio.isSelected()) {
                calculateHoseVolume();
            }
        });

        hoseTypeCombo.setOnAction(e -> {
            if (hoseRadio.isSelected()) calculateHoseVolume();
        });
    }

    /**
     * Configura los listeners para la medición por surco.
     */
    private void setupFurrowListeners() {
        furrowRadio.selectedProperty().addListener((obs, old, newVal) -> {
            furrowLengthInput.setDisable(!newVal);
            furrowWidthInput.setDisable(!newVal);
            furrowDepthCombo.setDisable(!newVal);
            if (newVal) calculateFurrowVolume();
        });

        furrowLengthInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                furrowLengthInput.setText(old);
            } else if (furrowRadio.isSelected()) {
                calculateFurrowVolume();
            }
        });

        furrowWidthInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                furrowWidthInput.setText(old);
            } else if (furrowRadio.isSelected()) {
                calculateFurrowVolume();
            }
        });

        furrowDepthCombo.setOnAction(e -> {
            if (furrowRadio.isSelected()) calculateFurrowVolume();
        });

    }

    /**
     * Configura los listeners para la medición por contenedor.
     */
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

// endregion

// region Exportación de Datos
    //TODO: Este método debería moverse a un futuro VolumeHandler/MeasurementHandler
    private String getMeasurementMethod() {
        if (containerRadio.isSelected()) return "container";
        if (pumpRadio.isSelected()) return "pump";
        if (hoseRadio.isSelected()) return "hose";
        if (furrowRadio.isSelected()) return "furrow";
        return "manual";
    }

    private String generateMeasurementDetailsJSON() {
        StringBuilder details = new StringBuilder("{");

        if (containerRadio.isSelected()) {
            details.append(String.format("\"container_type\": \"%s\",",
                            containerTypeCombo.getValue()))
                    .append(String.format("\"container_count\": %d",
                            containerCountSpinner.getValue()));
        }
        else if (pumpRadio.isSelected()) {
            details.append(String.format("\"pump_type\": \"%s\",",
                            pumpTypeCombo.getValue()))
                    .append(String.format("\"duration_minutes\": %.2f",
                            Double.parseDouble(pumpTimeInput.getText())));
        }
        else if (hoseRadio.isSelected()) {
            details.append(String.format("\"hose_type\": \"%s\",",
                            hoseTypeCombo.getValue()))
                    .append(String.format("\"duration_minutes\": %.2f",
                            Double.parseDouble(hoseTimeInput.getText())));
        }
        else if (furrowRadio.isSelected()) {
            details.append(String.format("\"length\": %.2f,",
                            Double.parseDouble(furrowLengthInput.getText())))
                    .append(String.format("\"width\": %.2f,",
                            Double.parseDouble(furrowWidthInput.getText())))
                    .append(String.format("\"depth_type\": \"%s\"",
                            furrowDepthCombo.getValue()));
        }

        details.append("}");
        return details.toString();
    }

// endregion

// region Acciones de UI
    //TODO: REORDENAR ESTOS CONTROLES O ACCIONES DE UI DESPUES
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
// endregion
}



