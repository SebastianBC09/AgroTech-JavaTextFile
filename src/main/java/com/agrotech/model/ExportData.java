package com.agrotech.model;

import java.util.Map;

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
) {}