package com.agrotech.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public record ExportData(
        String cropType,
        double waterTemperature,
        String temperatureUnit,
        double waterVolume,
        String volumeUnit,
        String measurementMethod,
        Map<String, Object> measurementDetails,
        double irrigationLevel,
        double soilHumidity,
        double airTemperature,
        double airHumidity
) {
    public Map<String, Object> toJsonFormat() {
        Map<String, Object> root = new HashMap<>();
        Map<String, Object> record = new HashMap<>();

        record.put("timestamp", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        ));

        record.put("crop_type", cropType);

        Map<String, Object> sensorData = new HashMap<>();
        sensorData.put("soil_humidity", soilHumidity);
        sensorData.put("air_temperature", airTemperature);
        sensorData.put("air_humidity", airHumidity);
        record.put("sensor_data", sensorData);

        Map<String, Object> waterData = new HashMap<>();
        waterData.put("temperature", waterTemperature);
        waterData.put("temperature_unit", temperatureUnit);
        waterData.put("volume", waterVolume);
        waterData.put("volume_unit", volumeUnit);
        record.put("water_data", waterData);

        Map<String, Object> measurement = new HashMap<>();
        measurement.put("method", measurementMethod);
        measurement.put("details", measurementDetails);
        record.put("measurement", measurement);

        record.put("irrigation_level", irrigationLevel);

        root.put("agricultural_record", record);
        return root;
    }
}