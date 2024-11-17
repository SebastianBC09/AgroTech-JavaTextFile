package com.agrotech.service;

import com.agrotech.model.VolumeCalculator;

public class FlowBasedVolumeCalculator implements VolumeCalculator {
    private final String flowType;
    private final double flowRate;  // L/h
    private final double minutes;

    private static final double MIN_MINUTES = 0.0;
    private static final double MAX_MINUTES = 1440.0; // 24 horas

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
    public String getDetailsJson() {
        return String.format(
                "{\"flow_type\":\"%s\",\"flow_rate\":%.2f,\"duration_minutes\":%.2f}",
                flowType, flowRate, minutes
        );
    }

    @Override
    public boolean isValid() {
        return flowType != null &&
                flowRate > 0.0 &&
                minutes >= MIN_MINUTES &&
                minutes <= MAX_MINUTES;
    }

    // Getters para facilitar testing y debug
    public String getFlowType() {
        return flowType;
    }

    public double getFlowRate() {
        return flowRate;
    }

    public double getMinutes() {
        return minutes;
    }
}
