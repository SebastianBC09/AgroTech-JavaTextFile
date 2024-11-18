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

    public SensorDataEnriched getLatestReading() {
        if (enrichedData == null || enrichedData.isEmpty()) {
            System.out.println("Advertencia: No hay datos disponibles");
            return null;
        }
        return enrichedData.get(enrichedData.size() - 1);
    }

}