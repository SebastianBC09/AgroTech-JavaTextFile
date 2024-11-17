package com.agrotech.service;

import com.agrotech.model.VolumeCalculator;
import java.util.Map;

public class ContainerVolumeCalculator implements VolumeCalculator {
    private final String containerType;
    private final int count;
    private final Map<String, Double> containerVolumes;

    public ContainerVolumeCalculator(String containerType, int count,
                                     Map<String, Double> containerVolumes) {
        this.containerType = containerType;
        this.count = count;
        this.containerVolumes = containerVolumes;
    }

    @Override
    public double calculateVolume() {
        if (!isValid()) {
            throw new IllegalStateException("Parámetros de contenedor inválidos");
        }
        double baseVolume = containerVolumes.getOrDefault(containerType, 0.0);
        return baseVolume * count;
    }

    @Override
    public String getDetailsJson() {
        return String.format(
                "{\"container_type\":\"%s\",\"container_count\":%d}",
                containerType, count
        );
    }

    @Override
    public boolean isValid() {
        return containerType != null &&
                containerVolumes.containsKey(containerType) &&
                count > 0;
    }
}
