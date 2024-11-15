package com.agrotech.controller;

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
    /**
     * Inicializa todos los componentes del dashboard.
     * Este método es llamado automáticamente por FXML después de la inyección.
     */
    @FXML
    public void initialize() {
        setupDateTime();
        setupTopPanel();
        setupCenterPanel();
        setupMainPanel();
        setupBottomPanel();

        // Deshabilitar controles hasta que se seleccione un método de medición
        disableAllVolumeControls();
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
        setupTemperatureControls();
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
        tempInput.setTooltip(new Tooltip("Temperatura del agua utilizada"));
        tempUnitCombo.setTooltip(new Tooltip("Unidad de temperatura"));
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
        sqlButton.setOnAction(e -> exportToSQL());
        nosqlButton.setOnAction(e -> exportToNoSQL());
        jsonButton.setOnAction(e -> exportToJSON());
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

            if (containerTypeCombo.getValue() == null &&
                    pumpTypeCombo.getValue() == null &&
                    hoseTypeCombo.getValue() == null &&
                    furrowDepthCombo.getValue() == null) {
                showAlert("Error", "Seleccione un método de medición");
                return false;
            }

        } catch (NumberFormatException e) {
            showAlert("Error", "Los valores numéricos son inválidos");
            return false;
        }

        return true;
    }
// endregion
    // region Control de Temperatura
    /**
     * Configura los controles de temperatura.
     */
    private void setupTemperatureControls() {
        // Configurar unidades de temperatura
        tempUnitCombo.getItems().addAll("°C", "°F");
        tempUnitCombo.setValue("°C");

        // Configurar validación de entrada
        setupTemperatureValidation();

        // Configurar conversión automática
        tempUnitCombo.setOnAction(e -> convertTemperature());

        // Tooltips
        tempInput.setTooltip(new Tooltip("Rango permitido: 0-50°C / 32-122°F"));
    }

    /**
     * Configura la validación de entrada para temperatura.
     */
    private void setupTemperatureValidation() {
        tempInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("-?\\d*\\.?\\d*")) {
                tempInput.setText(old);
                return;
            }

            try {
                if (!newVal.isEmpty()) {
                    double temp = Double.parseDouble(newVal);
                    boolean isValid = tempUnitCombo.getValue().equals("°C") ?
                            temp >= 0 && temp <= 50 :
                            temp >= 32 && temp <= 122;

                    if (!isValid) {
                        tempInput.setText(old);
                    }
                }
            } catch (NumberFormatException ignored) {
                tempInput.setText(old);
            }
        });
    }

    /**
     * Configura temperatura fría predefinida.
     */
    @FXML
    private void setTempFria() {
        tempInput.setText("15");
        tempUnitCombo.setValue("°C");
    }

    /**
     * Configura temperatura templada predefinida.
     */
    @FXML
    private void setTempTemplada() {
        tempInput.setText("20");
        tempUnitCombo.setValue("°C");
    }

    /**
     * Configura temperatura caliente predefinida.
     */
    @FXML
    private void setTempCaliente() {
        tempInput.setText("35");
        tempUnitCombo.setValue("°C");
    }

    /**
     * Convierte la temperatura entre Celsius y Fahrenheit.
     */
    private void convertTemperature() {
        if (tempInput.getText().isEmpty()) return;

        try {
            double temp = Double.parseDouble(tempInput.getText());
            double converted = tempUnitCombo.getValue().equals("°C") ?
                    (temp - 32) * 5/9 :  // F a C
                    temp * 9/5 + 32;     // C a F

            tempInput.setText(String.format("%.1f", converted));
        } catch (NumberFormatException ignored) {}
    }
// endregion

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
        measureType.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                updateVolumeInputs((RadioButton) newVal);
            }
        });

        setupMeasurementTypeListeners();
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

        // Deshabilitar todos los controles primero
        disableAllVolumeControls();

        // Habilitar y calcular según el tipo seleccionado
        switch (selectedButton.getId()) {
            case "containerRadio" -> {
                enableContainerControls();
                calculateContainerVolume();
            }
            case "pumpRadio" -> {
                enablePumpControls();
                calculatePumpVolume();
            }
            case "hoseRadio" -> {
                enableHoseControls();
                calculateHoseVolume();
            }
            case "furrowRadio" -> {
                enableFurrowControls();
                calculateFurrowVolume();
            }
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
            String currentUnit = volUnitCombo.getValue();

            // Convertir todo a litros primero
            double volumeInLiters = switch (currentUnit) {
                case "mL" -> volume / 1000;
                case "m³" -> volume * 1000;
                default -> volume; // ya está en litros
            };

            // Convertir a la unidad seleccionada
            double convertedVolume = switch (currentUnit) {
                case "mL" -> volumeInLiters * 1000;
                case "m³" -> volumeInLiters / 1000;
                default -> volumeInLiters;
            };

            volInput.setText(String.format("%.2f", convertedVolume));

        } catch (NumberFormatException ignored) {
            volInput.setText("0.00");
        }
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
        containerRadio.setDisable(true);
        containerTypeCombo.setDisable(true);
        containerCountSpinner.setDisable(true);

        // Controles de bomba
        pumpRadio.setDisable(true);
        pumpTypeCombo.setDisable(true);
        pumpTimeInput.setDisable(true);

        // Controles de manguera
        hoseRadio.setDisable(true);
        hoseTypeCombo.setDisable(true);
        hoseTimeInput.setDisable(true);

        // Controles de surco
        furrowRadio.setDisable(true);
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
        pumpTypeCombo.setOnAction(e -> {
            if (pumpRadio.isSelected()) calculatePumpVolume();
        });

        pumpTimeInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                pumpTimeInput.setText(old);
                return;
            }
            if (pumpRadio.isSelected()) calculatePumpVolume();
        });
    }

    /**
     * Configura los listeners para la medición por manguera.
     */
    private void setupHoseListeners() {
        hoseTypeCombo.setOnAction(e -> {
            if (hoseRadio.isSelected()) calculateHoseVolume();
        });

        hoseTimeInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                hoseTimeInput.setText(old);
                return;
            }
            if (hoseRadio.isSelected()) calculateHoseVolume();
        });
    }

    /**
     * Configura los listeners para la medición por surco.
     */
    private void setupFurrowListeners() {
        furrowDepthCombo.setOnAction(e -> {
            if (furrowRadio.isSelected()) calculateFurrowVolume();
        });

        // Listener para largo del surco
        furrowLengthInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                furrowLengthInput.setText(old);
                return;
            }
            if (furrowRadio.isSelected()) calculateFurrowVolume();
        });

        // Listener para ancho del surco
        furrowWidthInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                furrowWidthInput.setText(old);
                return;
            }
            if (furrowRadio.isSelected()) calculateFurrowVolume();
        });
    }

    /**
     * Configura los listeners para la medición por contenedor.
     */
    private void setupContainerListeners() {
        containerTypeCombo.setOnAction(e -> {
            if (containerRadio.isSelected()) calculateContainerVolume();
        });

        containerCountSpinner.valueProperty().addListener((obs, old, newVal) -> {
            if (containerRadio.isSelected()) calculateContainerVolume();
        });
    }

// endregion

    // region Exportación de Datos
    /**
     * Genera y guarda un script SQL con los datos actuales.
     */
    private void exportToSQL() {
        try {
            StringBuilder sql = generateSQLScript();
            saveToFile(sql.toString(), "Script SQL", "sql");
        } catch (Exception e) {
            showAlert("Error", "Error al generar script SQL: " + e.getMessage());
        }
    }

    /**
     * Genera el script SQL para la creación de tabla e inserción de datos.
     * @return StringBuilder con el script SQL
     */
    private StringBuilder generateSQLScript() {
        StringBuilder sql = new StringBuilder();

        // Crear tabla
        sql.append("""
        CREATE TABLE IF NOT EXISTS agricultural_records (
            id SERIAL PRIMARY KEY,
            record_date TIMESTAMP,
            crop_type VARCHAR(50),
            sensor_data JSONB,
            water_temperature DECIMAL(5,2),
            water_temperature_unit VARCHAR(2),
            water_volume DECIMAL(10,2),
            water_volume_unit VARCHAR(5),
            measurement_method VARCHAR(50),
            measurement_details JSONB,
            irrigation_level INTEGER,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );""");

        // Insertar datos actuales
        sql.append("""
            INSERT INTO agricultural_records (
                record_date, crop_type, sensor_data,
                water_temperature, water_temperature_unit,
                water_volume, water_volume_unit,
                measurement_method, measurement_details,
                irrigation_level
            ) VALUES (
            """);

        // Datos principales
        sql.append(String.format("    TIMESTAMP '%s',\n",
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .append(String.format("    '%s',\n", cropTypeCombo.getValue()))
                .append("    '{")
                .append("\"soil_humidity\": 0.00,")
                .append("\"air_temperature\": 0.00,")
                .append("\"air_humidity\": 0.00")
                .append("}',\n")
                .append(String.format("    %.2f,\n", Double.parseDouble(tempInput.getText())))
                .append(String.format("    '%s',\n", tempUnitCombo.getValue()))
                .append(String.format("    %.2f,\n", Double.parseDouble(volInput.getText())))
                .append(String.format("    '%s',\n", volUnitCombo.getValue()))
                .append(String.format("    '%s',\n", getMeasurementMethod()))
                .append("    ").append(generateMeasurementDetailsJSON()).append(",\n")
                .append(String.format("    %.0f\n", approximateVolSlider.getValue()))
                .append(");");

        return sql;
    }

    /**
     * Genera y guarda un script NoSQL (MongoDB) con los datos actuales.
     */
    private void exportToNoSQL() {
        try {
            StringBuilder nosql = generateNoSQLScript();
            saveToFile(nosql.toString(), "Script MongoDB", "js");
        } catch (Exception e) {
            showAlert("Error", "Error al generar script NoSQL: " + e.getMessage());
        }
    }

    /**
     * Genera el script NoSQL para MongoDB.
     * @return StringBuilder con el script NoSQL
     */
    private StringBuilder generateNoSQLScript() {
        StringBuilder nosql = new StringBuilder();

        nosql.append("db.agricultural_records.insertOne({\n")
                .append(String.format("    timestamp: ISODate(\"%s\"),\n",
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .append(String.format("    crop_type: \"%s\",\n", cropTypeCombo.getValue()))
                .append("    sensor_data: {\n")
                .append("        soil_humidity: 0.00,\n")
                .append("        air_temperature: 0.00,\n")
                .append("        air_humidity: 0.00\n")
                .append("    },\n")
                .append("    water_data: {\n")
                .append(String.format("        temperature: %.2f,\n",
                        Double.parseDouble(tempInput.getText())))
                .append(String.format("        temperature_unit: \"%s\",\n",
                        tempUnitCombo.getValue()))
                .append(String.format("        volume: %.2f,\n",
                        Double.parseDouble(volInput.getText())))
                .append(String.format("        volume_unit: \"%s\"\n",
                        volUnitCombo.getValue()))
                .append("    },\n")
                .append("    measurement: {\n")
                .append(String.format("        method: \"%s\",\n", getMeasurementMethod()))
                .append("        details: ").append(generateMeasurementDetailsJSON()).append("\n")
                .append("    },\n")
                .append(String.format("    irrigation_level: %.0f,\n",
                        approximateVolSlider.getValue()))
                .append("    created_at: new Date()\n")
                .append("});");

        return nosql;
    }

    /**
     * Genera y guarda un archivo JSON con los datos actuales.
     */
    private void exportToJSON() {
        try {
            StringBuilder json = generateJSONData();
            saveToFile(json.toString(), "Datos JSON", "json");
        } catch (Exception e) {
            showAlert("Error", "Error al generar JSON: " + e.getMessage());
        }
    }

    /**
     * Genera la estructura JSON de los datos.
     * @return StringBuilder con el JSON
     */
    private StringBuilder generateJSONData() {
        StringBuilder json = new StringBuilder();

        json.append("{\n")
                .append("    \"agricultural_record\": {\n")
                .append(String.format("        \"timestamp\": \"%s\",\n",
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .append(String.format("        \"crop_type\": \"%s\",\n",
                        cropTypeCombo.getValue()))
                .append("        \"sensor_data\": {\n")
                .append("            \"soil_humidity\": 0.00,\n")
                .append("            \"air_temperature\": 0.00,\n")
                .append("            \"air_humidity\": 0.00\n")
                .append("        },\n")
                .append("        \"water_data\": {\n")
                .append(String.format("            \"temperature\": %.2f,\n",
                        Double.parseDouble(tempInput.getText())))
                .append(String.format("            \"temperature_unit\": \"%s\",\n",
                        tempUnitCombo.getValue()))
                .append(String.format("            \"volume\": %.2f,\n",
                        Double.parseDouble(volInput.getText())))
                .append(String.format("            \"volume_unit\": \"%s\"\n",
                        volUnitCombo.getValue()))
                .append("        },\n")
                .append("        \"measurement\": {\n")
                .append(String.format("            \"method\": \"%s\",\n",
                        getMeasurementMethod()))
                .append("            \"details\": ").append(generateMeasurementDetailsJSON())
                .append("\n        },\n")
                .append(String.format("        \"irrigation_level\": %.0f\n",
                        approximateVolSlider.getValue()))
                .append("    }\n")
                .append("}");

        return json;
    }

    /**
     * Obtiene el método de medición actualmente seleccionado.
     * @return String con el método de medición
     */
    private String getMeasurementMethod() {
        if (containerRadio.isSelected()) return "container";
        if (pumpRadio.isSelected()) return "pump";
        if (hoseRadio.isSelected()) return "hose";
        if (furrowRadio.isSelected()) return "furrow";
        return "manual";
    }

    /**
     * Genera el JSON con los detalles específicos del método de medición.
     * @return String con el JSON de detalles
     */
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

    /**
     * Guarda el contenido en un archivo.
     * @param content Contenido a guardar
     * @param description Descripción del tipo de archivo
     * @param extension Extensión del archivo
     */
    private void saveToFile(String content, String description, String extension) {
        if (!validateExport()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar " + description);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(description, "*." + extension)
        );

        File file = fileChooser.showSaveDialog(mainContainer.getScene().getWindow());
        if (file != null) {
            try {
                Files.writeString(file.toPath(), content);
                showAlert("Éxito", "Archivo generado correctamente en:\n" +
                        file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Error", "Error al guardar el archivo: " + e.getMessage());
            }
        }
    }
// endregion
}
