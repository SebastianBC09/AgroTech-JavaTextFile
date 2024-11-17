package com.agrotech.service;

import com.agrotech.model.VolumeCalculator;

public class FurrowVolumeCalculator implements VolumeCalculator {
    private final double length;    // metros
    private final double width;     // metros
    private final double depth;     // metros

    private static final double MIN_DIMENSION = 0.1;  // 10 cm
    private static final double MAX_LENGTH = 100.0;   // 100 metros
    private static final double MAX_WIDTH = 5.0;      // 5 metros
    private static final double MAX_DEPTH = 0.3;      // 30 cm

    public FurrowVolumeCalculator(double length, double width, double depth) {
        this.length = length;
        this.width = width;
        this.depth = depth;
    }

    @Override
    public double calculateVolume() {
        if (!isValid()) {
            throw new IllegalStateException("Dimensiones de surco inválidas");
        }
        // Cálculo en metros cúbicos y conversión a litros
        return length * width * depth * 1000.0;
    }

    @Override
    public String getDetailsJson() {
        return String.format(
                "{\"length\":%.2f,\"width\":%.2f,\"depth\":%.2f}",
                length, width, depth
        );
    }

    @Override
    public boolean isValid() {
        return isValidDimension(length, MIN_DIMENSION, MAX_LENGTH) &&
                isValidDimension(width, MIN_DIMENSION, MAX_WIDTH) &&
                isValidDimension(depth, 0.01, MAX_DEPTH);  // profundidad mínima 1cm
    }

    private boolean isValidDimension(double value, double min, double max) {
        return value >= min && value <= max;
    }

    // Getters para facilitar testing y debug
    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getDepth() {
        return depth;
    }
}
