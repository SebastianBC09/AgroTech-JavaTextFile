package com.agrotech.model;

import java.time.LocalDateTime;

public record SensorDataEnriched(
        LocalDateTime timestamp,
        double soilHumidity,
        double airTemperature,
        double airHumidity,
        boolean irrigationStatus,
        String cropType,
        WaterData waterData,
        double irrigationLevel
) {
    public static SensorDataEnriched fromBasicData(SensorData basic) {
        return new SensorDataEnriched(
                basic.timestamp(),
                basic.soilHumidity(),
                basic.airTemperature(),
                basic.airHumidity(),
                basic.irrigationStatus(),
                null,
                new WaterData(basic.airTemperature(), "°C", 0.0, "L"), // valores por defecto
                50.0
        );
    }
}