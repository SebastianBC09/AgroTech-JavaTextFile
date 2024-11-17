package com.agrotech.handler;

import com.agrotech.service.TemperatureService;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

public class TemperatureHandler {
    private final TemperatureService temperatureService;
    private final TextField tempInput;
    private final ComboBox<String> tempUnitCombo;

    public TemperatureHandler(TextField tempInput, ComboBox<String> tempUnitCombo) {
        this.temperatureService = new TemperatureService();
        this.tempInput = tempInput;
        this.tempUnitCombo = tempUnitCombo;
        setupControls();
    }

    public void setupControls() {
        tempUnitCombo.getItems().addAll("°C", "°F");
        tempUnitCombo.setValue("°C");

        tempInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("-?\\d*\\.?\\d*")) {
                tempInput.setText(old);
                return;
            }
            validateTemperatureInput(old, newVal);
        });

        tempUnitCombo.valueProperty().addListener((obs, oldUnit, newUnit) -> {
            if (oldUnit != null && !oldUnit.equals(newUnit) && !tempInput.getText().isEmpty()) {
                convertTemperature(oldUnit, newUnit);
            }
        });
        tempInput.setTooltip(new Tooltip("Rango permitido: 0-50°C / 32-122°F"));
    }

    private void validateTemperatureInput(String oldValue, String newValue) {
        try {
            if (!newValue.isEmpty()) {
                double temp = Double.parseDouble(newValue);
                String unit = tempUnitCombo.getValue();

                if (!temperatureService.isValidTemperature(temp, unit)) {
                    tempInput.setText(oldValue);
                }
            }
        } catch (NumberFormatException e) {
            tempInput.setText(oldValue);
        }
    }

    public void setPresetTemperature(String preset) {
        double temp = temperatureService.getPresetTemperature(preset);
        tempInput.setText(String.valueOf(temp));
        tempUnitCombo.setValue("°C");
    }

    private void convertTemperature(String fromUnit, String toUnit) {
        try {
            double temp = Double.parseDouble(tempInput.getText());
            double converted = temperatureService.convertTemperature(temp, fromUnit, toUnit);
            tempInput.setText(String.format("%.1f", converted));
        } catch (NumberFormatException e) {
            System.err.println("Error en conversión: " + e.getMessage());
        }
    }

    public double getCurrentTemperature() {
        try {
            return Double.parseDouble(tempInput.getText());
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