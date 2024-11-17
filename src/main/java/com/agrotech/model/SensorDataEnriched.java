package com.agrotech.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
                null,  // cropType inicialmente vacío
                new WaterData(basic.airTemperature(), "°C", 0.0, "L"), // valores por defecto
                50.0   // valor por defecto del slider
        );
    }

    public Map<String, Object> toExportFormat() {
        Map<String, Object> export = new HashMap<>();
        Map<String, Object> record = new HashMap<>();

        record.put("timestamp", timestamp.toString());
        record.put("crop_type", cropType);

        Map<String, Object> sensorData = new HashMap<>();
        sensorData.put("soil_humidity", soilHumidity);
        sensorData.put("air_temperature", airTemperature);
        sensorData.put("air_humidity", airHumidity);
        record.put("sensor_data", sensorData);

        Map<String, Object> waterInfo = new HashMap<>();
        waterInfo.put("temperature", waterData.temperature());
        waterInfo.put("temperature_unit", waterData.temperatureUnit());
        waterInfo.put("volume", waterData.volume());
        waterInfo.put("volume_unit", waterData.volumeUnit());
        record.put("water_data", waterInfo);

        record.put("irrigation_level", irrigationLevel);

        export.put("agricultural_record", record);
        return export;
    }
}