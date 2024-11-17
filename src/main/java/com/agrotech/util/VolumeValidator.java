package com.agrotech.util;

public class VolumeValidator {
    public static boolean isValidVolume(double volume) {
        return volume >= 0 && !Double.isInfinite(volume);
    }

    public static double validateAndConvertVolume(String volumeStr) throws IllegalArgumentException {
        try {
            double volume = Double.parseDouble(volumeStr);
            if (!isValidVolume(volume)) {
                throw new IllegalArgumentException("El volumen debe ser un número positivo válido");
            }
            return volume;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Por favor, ingrese un número válido");
        }
    }
}
