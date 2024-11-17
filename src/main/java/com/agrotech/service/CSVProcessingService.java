package com.agrotech.service;

import com.agrotech.exception.CSVProcessingException;
import com.agrotech.model.SensorData;
import java.io.*;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;

public class CSVProcessingService {
    private List<SensorData> processedData;

    public CSVProcessingService() {
        this.processedData = new ArrayList<>();
    }

    public boolean processCSVFile(File file, Consumer<Double> progressCallback)
            throws CSVProcessingException {
        processedData.clear(); // Limpiar datos anteriores

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<String> lines = reader.lines().toList();
            int totalLines = lines.size() - 1; // Excluir header

            // Saltar el header
            boolean isFirstLine = true;
            int currentLine = 0;

            for (String line : lines) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                try {
                    SensorData data = parseLine(line);
                    processedData.add(data);

                    currentLine++;
                    if (progressCallback != null) {
                        progressCallback.accept((double) currentLine / totalLines);
                    }
                } catch (Exception e) {
                    throw new CSVProcessingException("Error en línea " + (currentLine + 1) + ": " + e.getMessage());
                }
            }

            return true;
        } catch (IOException e) {
            throw new CSVProcessingException("Error al leer el archivo: " + e.getMessage());
        }
    }

    private SensorData parseLine(String line) throws CSVProcessingException {
        String[] parts = line.split(",");
        if (parts.length != 5) {
            throw new CSVProcessingException(
                    String.format("Formato inválido: se esperaban 5 campos pero se encontraron %d", parts.length)
            );
        }

        try {
            return SensorData.fromCsvLine(line);
        } catch (Exception e) {
            throw new CSVProcessingException("Error al parsear línea: " + e.getMessage());
        }
    }

    public List<SensorData> getProcessedData() {
        return new ArrayList<>(processedData); // Retorna una copia para evitar modificaciones externas
    }

    public String getDataSummary() {
        if (processedData.isEmpty()) {
            return "No hay datos procesados";
        }

        return String.format("Datos procesados: %d registros", processedData.size());
    }
}

