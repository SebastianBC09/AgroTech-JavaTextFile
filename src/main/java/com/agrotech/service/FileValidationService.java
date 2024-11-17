package com.agrotech.service;

import com.agrotech.exception.FileValidationException;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileValidationService {
    private static final Set<String> REQUIRED_HEADERS = new HashSet<>(Arrays.asList(
            "timestamp", "soil_humidity", "air_temperature", "air_humidity", "irrigation_status"
    ));

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public boolean validateFile(File file) throws FileValidationException {
        validateFileBasics(file);
        validateFileSize(file);
        validateFileStructure(file);

        return true;
    }

    private void validateFileBasics(File file) throws FileValidationException {
        if (file == null) {
            throw new FileValidationException("El archivo es nulo");
        }
        if (!file.exists()) {
            throw new FileValidationException("El archivo no existe");
        }
        if (!file.isFile()) {
            throw new FileValidationException("La ruta no corresponde a un archivo");
        }
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            throw new FileValidationException("El archivo debe tener extensión .csv");
        }
    }

    private void validateFileSize(File file) throws FileValidationException {
        if (file.length() == 0) {
            throw new FileValidationException("El archivo está vacío");
        }
        if (file.length() > MAX_FILE_SIZE) {
            throw new FileValidationException(
                    String.format("El archivo excede el tamaño máximo permitido de %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }
    }

    private void validateFileStructure(File file) throws FileValidationException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Validar headers
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new FileValidationException("El archivo no contiene headers");
            }

            Set<String> headers = new HashSet<>(Arrays.asList(headerLine.toLowerCase().split(",")));
            for (String requiredHeader : REQUIRED_HEADERS) {
                if (!headers.contains(requiredHeader)) {
                    throw new FileValidationException(
                            String.format("Falta la columna requerida: %s", requiredHeader)
                    );
                }
            }

            // Validar que haya al menos una línea de datos
            if (reader.readLine() == null) {
                throw new FileValidationException("El archivo no contiene datos");
            }

        } catch (IOException e) {
            throw new FileValidationException("Error al leer el archivo: " + e.getMessage());
        }
    }
}
