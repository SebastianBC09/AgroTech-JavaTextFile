package com.agrotech.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record SensorData(
        LocalDateTime timestamp,
        double soilHumidity,
        double airTemperature,
        double airHumidity,
        boolean irrigationStatus
) {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static SensorData fromCsvLine(String line) {
        String[] parts = line.split(",");
        return new SensorData(
                LocalDateTime.parse(parts[0].trim(), FORMATTER),
                Double.parseDouble(parts[1].trim()),
                Double.parseDouble(parts[2].trim()),
                Double.parseDouble(parts[3].trim()),
                "1".equals(parts[4].trim())
        );
    }
}