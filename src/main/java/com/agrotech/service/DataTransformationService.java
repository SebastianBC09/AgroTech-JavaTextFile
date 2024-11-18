package com.agrotech.service;

import com.agrotech.model.*;
import java.util.List;
import java.util.stream.Collectors;

public class DataTransformationService {
    private static DataTransformationService instance;
    private List<SensorDataEnriched> enrichedData;

    private DataTransformationService() {}

    public static DataTransformationService getInstance() {
        if (instance == null) {
            instance = new DataTransformationService();
        }
        return instance;
    }

    public void enrichSensorData(List<SensorData> basicData) {
        System.out.println("Enriqueciendo datos de " + basicData.size() + " registros");

        enrichedData = basicData.stream()
                .map(SensorDataEnriched::fromBasicData)
                .collect(Collectors.toList());

        System.out.println("Datos enriquecidos completado");
    }

    public List<SensorDataEnriched> getEnrichedData() {
        return enrichedData;
    }

    public SensorDataEnriched getLatestReading() {
        if (enrichedData == null || enrichedData.isEmpty()) {
            System.out.println("Advertencia: No hay datos disponibles");
            return null;
        }
        return enrichedData.get(enrichedData.size() - 1);
    }

    public void updateWaterData(int index, WaterData waterData) {
        if (enrichedData != null && index < enrichedData.size()) {
            SensorDataEnriched original = enrichedData.get(index);
            enrichedData.set(index, new SensorDataEnriched(
                    original.timestamp(),
                    original.soilHumidity(),
                    original.airTemperature(),
                    original.airHumidity(),
                    original.irrigationStatus(),
                    original.cropType(),
                    waterData,
                    original.irrigationLevel()
            ));
            System.out.println("Datos de agua actualizados para el índice: " + index);
        }
    }

    public void updateCropType(String cropType) {
        if (enrichedData != null) {
            enrichedData = enrichedData.stream()
                    .map(data -> new SensorDataEnriched(
                            data.timestamp(),
                            data.soilHumidity(),
                            data.airTemperature(),
                            data.airHumidity(),
                            data.irrigationStatus(),
                            cropType,
                            data.waterData(),
                            data.irrigationLevel()
                    ))
                    .collect(Collectors.toList());
            System.out.println("Tipo de cultivo actualizado a: " + cropType);
        }
    }
}