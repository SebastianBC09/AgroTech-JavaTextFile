package com.agrotech.service;

import com.agrotech.model.VolumeCalculator;

public class FurrowVolumeCalculator implements VolumeCalculator {
    private final double length;
    private final double width;
    private final double depth;

    private static final double MIN_DIMENSION = 0.1;
    private static final double MAX_LENGTH = 100.0;
    private static final double MAX_WIDTH = 5.0;
    private static final double MAX_DEPTH = 0.3;

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
        return length * width * depth * 1000.0;
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
}
