package com.agrotech.service;

import com.agrotech.model.MeasurementType;
import com.agrotech.model.VolumeCalculator;
import com.agrotech.model.VolumeUnit;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class MeasurementService {
    // Constantes movidas desde el DashboardController
    private final Map<String, Double> containerVolumes = Map.of(
            "Balde (20L)", 20.0,
            "Tanque (200L)", 200.0,
            "Bidón (5L)", 5.0
    );

    private final Map<String, Double> pumpFlowRates = Map.of(
            "Bomba 1HP (3600 L/h)", 3600.0,
            "Bomba 2HP (7200 L/h)", 7200.0,
            "Motobomba (10000 L/h)", 10000.0
    );

    private final Map<String, Double> hoseFlowRates = Map.of(
            "Manguera 1/2\" (500 L/h)", 500.0,
            "Manguera 3/4\" (1000 L/h)", 1000.0,
            "Manguera 1\" (2000 L/h)", 2000.0
    );

    private final Map<String, Double> furrowDepths = Map.of(
            "Bajo (5cm)", 0.05,
            "Medio (10cm)", 0.10,
            "Alto (15cm)", 0.15
    );

    public VolumeCalculator createCalculator(MeasurementType type, Map<String, Object> params) {
        return switch (type) {
            case CONTAINER -> createContainerCalculator(params);
            case PUMP -> createFlowBasedCalculator(params, pumpFlowRates);
            case HOSE -> createFlowBasedCalculator(params, hoseFlowRates);
            case FURROW -> createFurrowCalculator(params);
            case MANUAL -> throw new IllegalArgumentException("Medición manual no requiere calculador");
        };
    }

    public Map<String, Double> getAvailableOptions(MeasurementType type) {
        return switch (type) {
            case CONTAINER -> containerVolumes;
            case PUMP -> pumpFlowRates;
            case HOSE -> hoseFlowRates;
            case FURROW -> furrowDepths;
            default -> Collections.emptyMap();
        };
    }

    private VolumeCalculator createContainerCalculator(Map<String, Object> params) {
        String type = (String) params.get("containerType");
        int count = (Integer) params.get("count");
        return new ContainerVolumeCalculator(type, count, containerVolumes);
    }

    private VolumeCalculator createFlowBasedCalculator(Map<String, Object> params,
                                                       Map<String, Double> flowRates) {
        String type = (String) params.get("flowType");
        double minutes = (Double) params.get("minutes");
        return new FlowBasedVolumeCalculator(type, flowRates.get(type), minutes);
    }

    private VolumeCalculator createFurrowCalculator(Map<String, Object> params) {
        double length = (Double) params.get("length");
        double width = (Double) params.get("width");
        String depthType = (String) params.get("depthType");
        double depth = furrowDepths.get(depthType);
        return new FurrowVolumeCalculator(length, width, depth);
    }

    public double convertVolume(double value, VolumeUnit fromUnit, VolumeUnit toUnit) {
        // Validación de entrada
        if (fromUnit == null || toUnit == null) {
            throw new IllegalArgumentException("Las unidades no pueden ser null");
        }

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Valor no válido para la conversión");
        }

        // Si las unidades son iguales, no hay necesidad de convertir
        if (fromUnit == toUnit) {
            return value;
        }

        // Convertir a litros primero
        double liters = value * fromUnit.getLiterConversionFactor();

        // Convertir de litros a la unidad destino
        double result = liters / toUnit.getLiterConversionFactor();

        // Debug
        System.out.println(String.format("Conversión: %.2f %s -> %.2f %s",
                value, fromUnit.getSymbol(), result, toUnit.getSymbol()));

        return result;
    }
}



