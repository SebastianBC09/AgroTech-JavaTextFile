package com.agrotech.model;

public enum VolumeUnit {
    LITER("L", 1.0),
    MILLILITER("mL", 0.001),
    CUBIC_METER("m³", 1000.0);

    private final String symbol;
    private final double literConversionFactor;

    VolumeUnit(String symbol, double literConversionFactor) {
        this.symbol = symbol;
        this.literConversionFactor = literConversionFactor;
    }

    public String getSymbol() {
        return symbol;
    }

    public double toLiters(double value) {
        return value * literConversionFactor;
    }

    public double fromLiters(double liters) {
        return liters / literConversionFactor;
    }

    public static VolumeUnit fromSymbol(String symbol) {
        for (VolumeUnit unit : values()) {
            if (unit.symbol.equals(symbol)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unidad de volumen no válida: " + symbol);
    }
}
