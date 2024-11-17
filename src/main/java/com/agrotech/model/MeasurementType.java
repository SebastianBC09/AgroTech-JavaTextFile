package com.agrotech.model;

public enum MeasurementType {
    CONTAINER("container", "Por Contenedores"),
    PUMP("pump", "Por Bomba"),
    HOSE("hose", "Por Manguera"),
    FURROW("furrow", "Por Surco"),
    MANUAL("manual", "Manual");

    private final String value;
    private final String displayName;

    MeasurementType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}
