package com.agrotech.service;

import com.agrotech.model.*;
import java.util.List;
import java.util.ArrayList;

public class DataTransformationService {
    private static DataTransformationService instance;
    private final List<SensorDataEnriched> enrichedData;

    private DataTransformationService() {
        this.enrichedData = new ArrayList<>();
    }

    public static DataTransformationService getInstance() {
        if (instance == null) {
            instance = new DataTransformationService();
        }
        return instance;
    }

    public void enrichSensorData(List<SensorData> basicData) {
        enrichedData.clear();
        enrichedData.addAll(basicData.stream()
                .map(SensorDataEnriched::fromBasicData)
                .toList());
    }

    public SensorDataEnriched getLatestReading() {
        if (enrichedData.isEmpty()) {
            return null;
        }
        return enrichedData.get(enrichedData.size() - 1);
    }
}