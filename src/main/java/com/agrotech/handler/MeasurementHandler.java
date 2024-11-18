package com.agrotech.handler;

import com.agrotech.model.MeasurementData;
import com.agrotech.model.MeasurementType;
import com.agrotech.model.VolumeCalculator;
import com.agrotech.model.VolumeUnit;
import com.agrotech.service.MeasurementService;
import javafx.scene.control.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MeasurementHandler {
    private final MeasurementService measurementService;
    private VolumeUnit currentUnit = VolumeUnit.LITER;

    private final TextField volInput;
    private final ComboBox<String> volUnitCombo;
    private final RadioButton containerRadio;
    private final ComboBox<String> containerTypeCombo;
    private final Spinner<Integer> containerCountSpinner;
    private final RadioButton pumpRadio;
    private final ComboBox<String> pumpTypeCombo;
    private final TextField pumpTimeInput;
    private final RadioButton hoseRadio;
    private final ComboBox<String> hoseTypeCombo;
    private final TextField hoseTimeInput;
    private final RadioButton furrowRadio;
    private final TextField furrowLengthInput;
    private final TextField furrowWidthInput;
    private final ComboBox<String> furrowDepthCombo;

    private Consumer<MeasurementData> onVolumeUpdated;

    public MeasurementHandler(
            TextField volInput,
            ComboBox<String> volUnitCombo,
            RadioButton containerRadio,
            ComboBox<String> containerTypeCombo,
            Spinner<Integer> containerCountSpinner,
            RadioButton pumpRadio,
            ComboBox<String> pumpTypeCombo,
            TextField pumpTimeInput,
            RadioButton hoseRadio,
            ComboBox<String> hoseTypeCombo,
            TextField hoseTimeInput,
            RadioButton furrowRadio,
            TextField furrowLengthInput,
            TextField furrowWidthInput,
            ComboBox<String> furrowDepthCombo
    ) {
        this.measurementService = new MeasurementService();

        if (volInput == null || volUnitCombo == null) {
            throw new IllegalArgumentException("Los componentes de volumen no pueden ser null");
        }

        this.volInput = volInput;
        this.volUnitCombo = volUnitCombo;
        this.containerRadio = containerRadio;
        this.containerTypeCombo = containerTypeCombo;
        this.containerCountSpinner = containerCountSpinner;
        this.pumpRadio = pumpRadio;
        this.pumpTypeCombo = pumpTypeCombo;
        this.pumpTimeInput = pumpTimeInput;
        this.hoseRadio = hoseRadio;
        this.hoseTypeCombo = hoseTypeCombo;
        this.hoseTimeInput = hoseTimeInput;
        this.furrowRadio = furrowRadio;
        this.furrowLengthInput = furrowLengthInput;
        this.furrowWidthInput = furrowWidthInput;
        this.furrowDepthCombo = furrowDepthCombo;

        initializeControls();
        setupListeners();
    }

    private void initializeControls() {
        // Inicializar ComboBox de unidades
        volUnitCombo.getItems().addAll("L", "mL", "m³");
        volUnitCombo.setValue("L");

        // Inicializar opciones para cada tipo de medición
        containerTypeCombo.getItems().addAll(
                measurementService.getAvailableOptions(MeasurementType.CONTAINER).keySet()
        );
        pumpTypeCombo.getItems().addAll(
                measurementService.getAvailableOptions(MeasurementType.PUMP).keySet()
        );
        hoseTypeCombo.getItems().addAll(
                measurementService.getAvailableOptions(MeasurementType.HOSE).keySet()
        );
        furrowDepthCombo.getItems().addAll(
                measurementService.getAvailableOptions(MeasurementType.FURROW).keySet()
        );

        // Configurar Spinner
        containerCountSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1)
        );
    }

    private void setupListeners() {
        // Listener para cambios en la selección del tipo de medición
        ToggleGroup measureType = containerRadio.getToggleGroup();
        measureType.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                updateVolumeInputs((RadioButton) newVal);
            }
        });

        // Configurar listeners específicos para cada tipo
        setupContainerListeners();
        setupPumpListeners();
        setupHoseListeners();
        setupFurrowListeners();

        // Listener para conversión de unidades
        volUnitCombo.setOnAction(e -> {
            String newUnitSymbol = volUnitCombo.getValue();
            if (newUnitSymbol != null && !newUnitSymbol.equals(currentUnit.getSymbol())) {
                convertVolume(currentUnit, VolumeUnit.fromSymbol(newUnitSymbol));
            }
        });

    }

    private void setupContainerListeners() {
        containerTypeCombo.setOnAction(e -> {
            if (containerRadio.isSelected()) calculateVolume(MeasurementType.CONTAINER);
        });
        containerCountSpinner.valueProperty().addListener((obs, old, newVal) -> {
            if (containerRadio.isSelected()) calculateVolume(MeasurementType.CONTAINER);
        });
    }

    private void setupPumpListeners() {
        pumpTypeCombo.setOnAction(e -> {
            if (pumpRadio.isSelected()) calculateVolume(MeasurementType.PUMP);
        });

        // Validación y cálculo automático al escribir tiempo
        pumpTimeInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                pumpTimeInput.setText(old);
            } else if (!newVal.isEmpty() && pumpRadio.isSelected()) {
                calculateVolume(MeasurementType.PUMP);
            }
        });
    }

    private void setupHoseListeners() {
        hoseTypeCombo.setOnAction(e -> {
            if (hoseRadio.isSelected()) calculateVolume(MeasurementType.HOSE);
        });

        // Validación y cálculo automático al escribir tiempo
        hoseTimeInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                hoseTimeInput.setText(old);
            } else if (!newVal.isEmpty() && hoseRadio.isSelected()) {
                calculateVolume(MeasurementType.HOSE);
            }
        });
    }

    private void setupFurrowListeners() {
        // Listener para cambios en profundidad
        furrowDepthCombo.setOnAction(e -> {
            if (furrowRadio.isSelected()) calculateVolume(MeasurementType.FURROW);
        });

        // Validación y cálculo automático para largo
        furrowLengthInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                furrowLengthInput.setText(old);
            } else if (!newVal.isEmpty() && furrowRadio.isSelected() &&
                    !furrowWidthInput.getText().isEmpty()) {
                calculateVolume(MeasurementType.FURROW);
            }
        });

        // Validación y cálculo automático para ancho
        furrowWidthInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                furrowWidthInput.setText(old);
            } else if (!newVal.isEmpty() && furrowRadio.isSelected() &&
                    !furrowLengthInput.getText().isEmpty()) {
                calculateVolume(MeasurementType.FURROW);
            }
        });
    }

    private void updateVolumeInputs(RadioButton selectedButton) {
        // Deshabilitar todos los controles primero
        disableAllControls();

        // Habilitar solo los controles correspondientes
        if (selectedButton == containerRadio) {
            enableContainerControls();
            if (isContainerInputValid()) {
                calculateVolume(MeasurementType.CONTAINER);
            }
        } else if (selectedButton == pumpRadio) {
            enablePumpControls();
            if (isPumpInputValid()) {
                calculateVolume(MeasurementType.PUMP);
            }
        } else if (selectedButton == hoseRadio) {
            enableHoseControls();
            if (isHoseInputValid()) {
                calculateVolume(MeasurementType.HOSE);
            }
        } else if (selectedButton == furrowRadio) {
            enableFurrowControls();
            if (isFurrowInputValid()) {
                calculateVolume(MeasurementType.FURROW);
            }
        }
    }

    private void disableAllControls() {
        // Contenedor
        containerTypeCombo.setDisable(true);
        containerCountSpinner.setDisable(true);

        // Bomba
        pumpTypeCombo.setDisable(true);
        pumpTimeInput.setDisable(true);

        // Manguera
        hoseTypeCombo.setDisable(true);
        hoseTimeInput.setDisable(true);

        // Surco
        furrowLengthInput.setDisable(true);
        furrowWidthInput.setDisable(true);
        furrowDepthCombo.setDisable(true);
    }

    private void enableContainerControls() {
        containerTypeCombo.setDisable(false);
        containerCountSpinner.setDisable(false);
    }

    private void enablePumpControls() {
        pumpTypeCombo.setDisable(false);
        pumpTimeInput.setDisable(false);
    }

    private void enableHoseControls() {
        hoseTypeCombo.setDisable(false);
        hoseTimeInput.setDisable(false);
    }

    private void enableFurrowControls() {
        furrowLengthInput.setDisable(false);
        furrowWidthInput.setDisable(false);
        furrowDepthCombo.setDisable(false);
    }

    private Map<String, Object> buildParameters(MeasurementType type) {
        Map<String, Object> params = new HashMap<>();

        switch (type) {
            case CONTAINER -> {
                params.put("containerType", containerTypeCombo.getValue());
                params.put("count", containerCountSpinner.getValue());
            }
            case PUMP -> {
                params.put("flowType", pumpTypeCombo.getValue());
                params.put("minutes", getDoubleValue(pumpTimeInput));
            }
            case HOSE -> {
                params.put("flowType", hoseTypeCombo.getValue());
                params.put("minutes", getDoubleValue(hoseTimeInput));
            }
            case FURROW -> {
                params.put("length", getDoubleValue(furrowLengthInput));
                params.put("width", getDoubleValue(furrowWidthInput));
                params.put("depthType", furrowDepthCombo.getValue());
            }
        }

        return params;
    }

    private boolean isContainerInputValid() {
        return containerTypeCombo.getValue() != null &&
                containerCountSpinner.getValue() != null;
    }

    private boolean isPumpInputValid() {
        return pumpTypeCombo.getValue() != null &&
                isValidDoubleInput(pumpTimeInput);
    }

    private boolean isHoseInputValid() {
        return hoseTypeCombo.getValue() != null &&
                isValidDoubleInput(hoseTimeInput);
    }

    private boolean isFurrowInputValid() {
        return isValidDoubleInput(furrowLengthInput) &&
                isValidDoubleInput(furrowWidthInput) &&
                furrowDepthCombo.getValue() != null;
    }

    private boolean isValidDoubleInput(TextField input) {
        try {
            if (input.getText().isEmpty()) return false;
            Double.parseDouble(input.getText());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double getDoubleValue(TextField input) {
        try {
            return Double.parseDouble(input.getText());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public void reset() {
        volInput.clear();
        volUnitCombo.setValue("L");
        disableAllControls();
        clearAllInputs();
    }

    private void clearAllInputs() {
        containerTypeCombo.setValue(null);
        containerCountSpinner.getValueFactory().setValue(1);
        pumpTypeCombo.setValue(null);
        pumpTimeInput.clear();
        hoseTypeCombo.setValue(null);
        hoseTimeInput.clear();
        furrowLengthInput.clear();
        furrowWidthInput.clear();
        furrowDepthCombo.setValue(null);
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Medición");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean validateCurrentInput() {
        try {
            MeasurementType type = getCurrentMeasurementType();
            if (type == null) {
                showError("Tipo de medición no seleccionado");
                return false;
            }

            return switch (type) {
                case CONTAINER -> validateContainerInput();
                case PUMP -> validatePumpInput();
                case HOSE -> validateHoseInput();
                case FURROW -> validateFurrowInput();
                case MANUAL -> validateManualInput();
            };
        } catch (Exception e) {
            showError("Error al validar los datos: " + e.getMessage());
            return false;
        }
    }

    private boolean validateContainerInput() {
        if (containerTypeCombo == null || containerCountSpinner == null) {
            showError("Componentes de contenedor no inicializados");
            return false;
        }
        if (!isContainerInputValid()) {
            showError("Por favor, seleccione un tipo de contenedor y cantidad");
            return false;
        }
        return true;
    }

    private boolean validatePumpInput() {
        if (pumpTypeCombo == null || pumpTimeInput == null) {
            showError("Componentes de bomba no inicializados");
            return false;
        }
        if (!isPumpInputValid()) {
            showError("Por favor, seleccione un tipo de bomba y tiempo válido");
            return false;
        }
        return true;
    }

    private boolean validateHoseInput() {
        if (hoseTypeCombo == null || hoseTimeInput == null) {
            showError("Componentes de manguera no inicializados");
            return false;
        }
        if (!isHoseInputValid()) {
            showError("Por favor, seleccione un tipo de manguera y tiempo válido");
            return false;
        }
        return true;
    }

    private boolean validateFurrowInput() {
        if (furrowLengthInput == null || furrowWidthInput == null || furrowDepthCombo == null) {
            showError("Componentes de surco no inicializados");
            return false;
        }
        if (!isFurrowInputValid()) {
            showError("Por favor, ingrese dimensiones válidas para el surco");
            return false;
        }
        return true;
    }

    private boolean validateManualInput() {
        if (volInput == null) {
            showError("Campo de volumen no inicializado");
            return false;
        }
        if (volInput.getText().isEmpty()) {
            showError("Por favor, ingrese un volumen válido");
            return false;
        }
        return true;
    }

    private void calculateVolume(MeasurementType type) {
        try {
            Map<String, Object> params = buildParameters(type);
            VolumeCalculator calculator = measurementService.createCalculator(type, params);

            if (calculator.isValid()) {
                double volume = calculator.calculateVolume();
                updateVolumeDisplay(volume, VolumeUnit.LITER);

                if (onVolumeUpdated != null) {
                    MeasurementData data = new MeasurementData(
                            type,
                            volume,
                            VolumeUnit.LITER,
                            params
                    );
                    onVolumeUpdated.accept(data);
                }
            }
        } catch (Exception e) {
            // Manejar errores
            System.err.println("Error al calcular volumen: " + e.getMessage());
        }
    }

    private void updateVolumeDisplay(double volume, VolumeUnit unit) {
        volInput.setText(String.format("%.2f", volume));
        volUnitCombo.setValue(unit.getSymbol());
    }

    private void convertVolume(VolumeUnit fromUnit, VolumeUnit toUnit) {
        if (volInput.getText().isEmpty()) return;

        try {
            double currentValue = Double.parseDouble(volInput.getText());

            // Realizar la conversión
            double converted = measurementService.convertVolume(currentValue, fromUnit, toUnit);

            // Aquí es donde cambiamos el formato de %.2f a %.3f
            volInput.setText(String.format("%.3f", converted));
            currentUnit = toUnit;

            // Debug
            System.out.println("Conversión: " + currentValue + " " + fromUnit.getSymbol() +
                    " -> " + converted + " " + toUnit.getSymbol());

            // Notificar el cambio si hay un listener
            if (onVolumeUpdated != null) {
                MeasurementData data = new MeasurementData(
                        getCurrentMeasurementType(),
                        converted,
                        toUnit,
                        buildParameters(getCurrentMeasurementType())
                );
                onVolumeUpdated.accept(data);
            }
        } catch (NumberFormatException e) {
            showError("Por favor, ingrese un valor numérico válido");
        } catch (IllegalArgumentException e) {
            showError("Error en la conversión: " + e.getMessage());
        }
    }

    public void setOnVolumeUpdated(Consumer<MeasurementData> callback) {
        this.onVolumeUpdated = callback;
    }

    public MeasurementData getCurrentMeasurement() {
        try {
            double volume = Double.parseDouble(volInput.getText());
            VolumeUnit unit = VolumeUnit.fromSymbol(volUnitCombo.getValue());
            MeasurementType type = getCurrentMeasurementType();
            Map<String, Object> params = buildParameters(type);

            return new MeasurementData(type, volume, unit, params);
        } catch (Exception e) {
            return null;
        }
    }

    private MeasurementType getCurrentMeasurementType() {
        try {
            if (containerRadio != null && containerRadio.isSelected()) return MeasurementType.CONTAINER;
            if (pumpRadio != null && pumpRadio.isSelected()) return MeasurementType.PUMP;
            if (hoseRadio != null && hoseRadio.isSelected()) return MeasurementType.HOSE;
            if (furrowRadio != null && furrowRadio.isSelected()) return MeasurementType.FURROW;
            return MeasurementType.MANUAL;
        } catch (Exception e) {
            System.err.println("Error al determinar el tipo de medición: " + e.getMessage());
            return MeasurementType.MANUAL; // Valor por defecto
        }
    }

}
