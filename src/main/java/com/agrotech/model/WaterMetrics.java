package com.agrotech.model;

public class WaterMetrics {
    private double totalWaterUsed;
    private double averageSoilHumidity;
    private double averageAirTemperature;
    private double averageAirHumidity;

    public WaterMetrics(double totalWaterUsed, double averageSoilHumidity, double averageAirTemperature, double averageAirHumidity) {
        this.totalWaterUsed = totalWaterUsed;
        this.averageSoilHumidity = averageSoilHumidity;
        this.averageAirTemperature = averageAirTemperature;
        this.averageAirHumidity = averageAirHumidity;
    }

    // Getters y setters para cada atributo
    // ...
}