package com.agrotech.handler;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class TemperatureHandler {
    private final TextField tempInput;
    private final ComboBox<String> tempUnitCombo;

    // Constantes para temperaturas predefinidas
    private static final double TEMP_FRIA = 15.0;
    private static final double TEMP_TEMPLADA = 20.0;
    private static final double TEMP_CALIENTE = 25.0;

    public TemperatureHandler(TextField tempInput, ComboBox<String> tempUnitCombo) {
        this.tempInput = tempInput;
        this.tempUnitCombo = tempUnitCombo;
        initializeControls();
    }

    private void initializeControls() {
        tempUnitCombo.getItems().addAll("°C", "°F");
        tempUnitCombo.setValue("°C");
        setupNumericValidation();
    }

    private void setupNumericValidation() {
        tempInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("-?\\d*\\.?\\d*")) {
                tempInput.setText(old);
            }
        });
    }

    public void setPresetTemperature(String preset) {
        double temperature = switch (preset.toLowerCase()) {
            case "fria" -> TEMP_FRIA;
            case "templada" -> TEMP_TEMPLADA;
            case "caliente" -> TEMP_CALIENTE;
            default -> throw new IllegalArgumentException("Preset de temperatura no válido");
        };

        tempInput.setText(String.format("%.1f", temperature));
        tempUnitCombo.setValue("°C");
    }

    public double getCurrentTemperature() {
        try {
            double temp = Double.parseDouble(tempInput.getText());
            if ("°F".equals(tempUnitCombo.getValue())) {
                return (temp - 32) * 5/9; // Convertir a Celsius
            }
            return temp;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public String getCurrentUnit() {
        return tempUnitCombo.getValue();
    }

    public boolean validateTemperature() {
        if (tempInput.getText() == null || tempInput.getText().isEmpty()) {
            return false;
        }

        try {
            double temp = Double.parseDouble(tempInput.getText());
            String unit = tempUnitCombo.getValue();

            // Rangos válidos de temperatura
            if ("°C".equals(unit)) {
                return temp >= 0 && temp <= 40;  // Rango razonable en Celsius
            } else {
                return temp >= 32 && temp <= 104; // Rango equivalente en Fahrenheit
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
}