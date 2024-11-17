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

    public void setPresetTemperature(String preset) {
        double temp = temperatureService.getPresetTemperature(preset);
        tempInput.setText(String.valueOf(temp));
        tempUnitCombo.setValue("°C");
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

    public void setupControls() {
        tempUnitCombo.getItems().addAll("°C", "°F");
        tempUnitCombo.setValue("°C");

        setupValidation();
        setupConversion();
        setupTooltips();
    }

    private void setupValidation() {
        tempInput.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("-?\\d*\\.?\\d*")) {
                tempInput.setText(old);
                return;
            }
            validateTemperatureInput(old, newVal);
        });
    }

    private void setupConversion() {
        tempUnitCombo.setOnAction(e -> convertTemperature());
    }

    private void setupTooltips() {
        tempInput.setTooltip(new Tooltip("Temperatura del agua utilizada"));
        tempUnitCombo.setTooltip(new Tooltip("Unidad de temperatura"));
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

    public void convertTemperature() {
        if (tempInput.getText().isEmpty()) return;

        try {
            double temp = Double.parseDouble(tempInput.getText());
            String fromUnit = tempUnitCombo.getPromptText();
            String toUnit = tempUnitCombo.getValue();

            double converted = temperatureService.convertTemperature(temp, fromUnit, toUnit);
            tempInput.setText(String.format("%.1f", converted));
            tempUnitCombo.setPromptText(toUnit);
        } catch (NumberFormatException ignored) {}
    }
}
