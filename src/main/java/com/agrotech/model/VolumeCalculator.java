package com.agrotech.model;

public interface VolumeCalculator {
    /**
     * Calcula el volumen basado en los parámetros específicos del tipo de medición.
     * @return volumen en litros
     */
    double calculateVolume();

    /**
     * Genera un JSON con los detalles específicos de la medición.
     * @return String en formato JSON con los detalles
     */
    String getDetailsJson();

    /**
     * Valida si los parámetros actuales son válidos para el cálculo.
     * @return true si los parámetros son válidos
     */
    boolean isValid();
}
