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
        if (type == null) {
            throw new IllegalArgumentException("El tipo de medición no puede ser null");
        }
        if (unit == null) {
            throw new IllegalArgumentException("La unidad no puede ser null");
        }
        if (volume < 0) {
            throw new IllegalArgumentException("El volumen no puede ser negativo");
        }
        details = details != null ? Collections.unmodifiableMap(details) : Collections.emptyMap();
    }

    @Override
    public String toString() {
        return String.format("%.2f %s (%s)", volume, unit.getSymbol(), type.getDisplayName());
    }
}
