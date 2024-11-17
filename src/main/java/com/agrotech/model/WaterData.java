package com.agrotech.model;

public record WaterData(
        double temperature,
        String temperatureUnit,
        double volume,
        String volumeUnit
) {}
