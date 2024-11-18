package com.agrotech.service;

import com.agrotech.model.VolumeCalculator;

public class FlowBasedVolumeCalculator implements VolumeCalculator {
    private final String flowType;
    private final double flowRate;
    private final double minutes;

    private static final double MIN_MINUTES = 0.0;
    private static final double MAX_MINUTES = 1440.0;

    public FlowBasedVolumeCalculator(String flowType, double flowRate, double minutes) {
        this.flowType = flowType;
        this.flowRate = flowRate;
        this.minutes = minutes;
    }

    @Override
    public double calculateVolume() {
        if (!isValid()) {
            throw new IllegalStateException("Parámetros de flujo inválidos");
        }
        return (flowRate / 60.0) * minutes;  // Convertir tasa de L/h a L/min
    }

    @Override
    public boolean isValid() {
        return flowType != null &&
                flowRate > 0.0 &&
                minutes >= MIN_MINUTES &&
                minutes <= MAX_MINUTES;
    }
}
