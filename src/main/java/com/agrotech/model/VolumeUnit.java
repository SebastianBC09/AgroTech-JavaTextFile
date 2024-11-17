package com.agrotech.model;

public enum VolumeUnit {
    MILLILITER("mL", 0.001),
    LITER("L", 1.0),
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

    public double getLiterConversionFactor() {
        return literConversionFactor;
    }

    public static VolumeUnit fromSymbol(String symbol) {
        for (VolumeUnit unit : values()) {
            if (unit.getSymbol().equals(symbol)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unidad de volumen no válida: " + symbol);
    }
}
