package com.agrotech.model;

import java.util.Map;
import java.util.Collections;

public record MeasurementData(
        MeasurementType type,
        double volume,
        VolumeUnit unit,
        Map<String, Object> details
) {
    public MeasurementData {
        // Validación defensiva
        if (type == null) {
            throw new IllegalArgumentException("El tipo de medición no puede ser null");
        }
        if (unit == null) {
            throw new IllegalArgumentException("La unidad no puede ser null");
        }
        if (volume < 0) {
            throw new IllegalArgumentException("El volumen no puede ser negativo");
        }
        // Asegurar que details sea inmutable
        details = details != null ? Collections.unmodifiableMap(details) : Collections.emptyMap();
    }

    public double getVolumeInLiters() {
        return volume * unit.getLiterConversionFactor();
    }

    public MeasurementData withUnit(VolumeUnit newUnit) {
        if (newUnit == null) {
            throw new IllegalArgumentException("La nueva unidad no puede ser null");
        }
        // Primero convertimos a litros
        double liters = getVolumeInLiters();
        // Luego convertimos de litros a la nueva unidad
        double newVolume = liters / newUnit.getLiterConversionFactor();
        return new MeasurementData(type, newVolume, newUnit, details);
    }

    public MeasurementData withVolume(double newVolume) {
        if (newVolume < 0) {
            throw new IllegalArgumentException("El volumen no puede ser negativo");
        }
        return new MeasurementData(type, newVolume, unit, details);
    }

    @Override
    public String toString() {
        return String.format("%.2f %s (%s)", volume, unit.getSymbol(), type.getDisplayName());
    }

    public double getVolumeIn(VolumeUnit targetUnit) {
        if (targetUnit == null) {
            throw new IllegalArgumentException("La unidad objetivo no puede ser null");
        }
        double liters = getVolumeInLiters();
        return liters / targetUnit.getLiterConversionFactor();
    }
}
