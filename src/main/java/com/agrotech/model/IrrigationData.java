package com.agrotech.model;

import java.time.LocalDateTime;

public class IrrigationData {
    private LocalDateTime timestamp;
    private double soilHumidity;
    private double airTemperature;
    private double airHumidity;
    private boolean irrigationStatus;

    public IrrigationData(LocalDateTime timestamp, double soilHumidity, double airTemperature, double airHumidity, boolean irrigationStatus) {
        this.timestamp = timestamp;
        this.soilHumidity = soilHumidity;
        this.airTemperature = airTemperature;
        this.airHumidity = airHumidity;
        this.irrigationStatus = irrigationStatus;
    }

    // Getters y setters para cada atributo
    // ...
}