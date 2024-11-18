// First Step: Extract Temperature Management
package com.agrotech.service;

public class TemperatureService {
    private static final double MIN_TEMP_CELSIUS = 0.0;
    private static final double MAX_TEMP_CELSIUS = 50.0;
    private static final double MIN_TEMP_FAHRENHEIT = 32.0;
    private static final double MAX_TEMP_FAHRENHEIT = 122.0;

    public boolean isValidTemperature(double temperature, String unit) {
        return switch (unit) {
            case "°C" -> temperature >= MIN_TEMP_CELSIUS && temperature <= MAX_TEMP_CELSIUS;
            case "°F" -> temperature >= MIN_TEMP_FAHRENHEIT && temperature <= MAX_TEMP_FAHRENHEIT;
            default -> false;
        };
    }

    public double convertTemperature(double value, String fromUnit, String toUnit) {
        if (fromUnit.equals(toUnit)) return value;

        return switch (toUnit) {
            case "°C" -> (value - 32) * 5.0/9.0;
            case "°F" -> (value * 9.0/5.0) + 32;
            default -> throw new IllegalArgumentException("Unidad de temperatura no soportada: " + toUnit);
        };
    }

    public double getPresetTemperature(String preset) {
        return switch (preset.toLowerCase()) {
            case "fria" -> 15.0;
            case "templada" -> 20.0;
            case "caliente" -> 35.0;
            default -> throw new IllegalArgumentException("Preset temperature not found: " + preset);
        };
    }
}