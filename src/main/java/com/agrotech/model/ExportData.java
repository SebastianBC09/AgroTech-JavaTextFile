package com.agrotech.model;

public class ExportData {
    private final String cropType;
    private final double waterTemperature;
    private final String temperatureUnit;
    private final double waterVolume;
    private final String volumeUnit;
    private final String measurementMethod;
    private final String measurementDetails;
    private final double irrigationLevel;

    // Constructor
    public ExportData(String cropType, double waterTemperature, String temperatureUnit,
                      double waterVolume, String volumeUnit, String measurementMethod,
                      String measurementDetails, double irrigationLevel) {
        this.cropType = cropType;
        this.waterTemperature = waterTemperature;
        this.temperatureUnit = temperatureUnit;
        this.waterVolume = waterVolume;
        this.volumeUnit = volumeUnit;
        this.measurementMethod = measurementMethod;
        this.measurementDetails = measurementDetails;
        this.irrigationLevel = irrigationLevel;
    }

    // Getters
    public String getCropType() { return cropType; }
    public double getWaterTemperature() { return waterTemperature; }
    public String getTemperatureUnit() { return temperatureUnit; }
    public double getWaterVolume() { return waterVolume; }
    public String getVolumeUnit() { return volumeUnit; }
    public String getMeasurementMethod() { return measurementMethod; }
    public String getMeasurementDetails() { return measurementDetails; }
    public double getIrrigationLevel() { return irrigationLevel; }
}
