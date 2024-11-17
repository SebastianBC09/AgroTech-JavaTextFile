package com.agrotech.handler;

import com.agrotech.model.ValidationResult;

public class ValidationHandler {
    private final TemperatureHandler temperatureHandler;
    private final MeasurementHandler measurementHandler;

    public ValidationHandler(TemperatureHandler temperatureHandler,
                             MeasurementHandler measurementHandler) {
        // Validar que los handlers no sean null
        if (temperatureHandler == null) {
            throw new IllegalArgumentException("TemperatureHandler no puede ser null");
        }
        if (measurementHandler == null) {
            throw new IllegalArgumentException("MeasurementHandler no puede ser null");
        }

        this.temperatureHandler = temperatureHandler;
        this.measurementHandler = measurementHandler;
    }

    public ValidationResult validateExportData(String cropType) {
        // Validar cultivo
        if (cropType == null || cropType.isEmpty()) {
            return ValidationResult.error("Seleccione un tipo de cultivo");
        }

        // Validar temperatura
        ValidationResult tempValidation = validateTemperature();
        if (!tempValidation.isValid()) {
            return tempValidation;
        }

        // Validar mediciones
        ValidationResult measurementValidation = validateMeasurement();
        if (!measurementValidation.isValid()) {
            return measurementValidation;
        }

        return ValidationResult.success();
    }

    private ValidationResult validateTemperature() {
        try {
            if (temperatureHandler == null) {
                return ValidationResult.error("Sistema de temperatura no inicializado");
            }
            if (!temperatureHandler.validateTemperature()) {
                return ValidationResult.error("Ingrese una temperatura válida para el agua");
            }
            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("Error al validar la temperatura: " + e.getMessage());
        }
    }
    private ValidationResult validateMeasurement() {
        try {
            if (measurementHandler == null) {
                return ValidationResult.error("Sistema de medición no inicializado");
            }
            if (!measurementHandler.validateCurrentInput()) {
                return ValidationResult.error("Los datos de medición son inválidos");
            }
            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("Error al validar la medición: " + e.getMessage());
        }
    }
}
