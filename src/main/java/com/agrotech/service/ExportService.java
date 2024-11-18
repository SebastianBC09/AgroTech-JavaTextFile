package com.agrotech.service;

import com.agrotech.model.ExportData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExportService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateSQLScript(ExportData data) {
        StringBuilder sql = new StringBuilder();

        // Crear tabla
        sql.append("""
        CREATE TABLE IF NOT EXISTS agricultural_records (
            id SERIAL PRIMARY KEY,
            record_date TIMESTAMP,
            crop_type VARCHAR(50),
            sensor_data JSONB,
            water_temperature DECIMAL(5,2),
            water_temperature_unit VARCHAR(2),
            water_volume DECIMAL(10,2),
            water_volume_unit VARCHAR(5),
            measurement_method VARCHAR(50),
            measurement_details JSONB,
            irrigation_level INTEGER,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );""");

        // Insertar datos
        sql.append("""
            INSERT INTO agricultural_records (
                record_date, crop_type, sensor_data,
                water_temperature, water_temperature_unit,
                water_volume, water_volume_unit,
                measurement_method, measurement_details,
                irrigation_level
            ) VALUES (
            """);

        sql.append(String.format("    TIMESTAMP '%s',\n",
                        LocalDateTime.now().format(DATE_TIME_FORMATTER)))
                .append(String.format("    '%s',\n", data.cropType()))
                .append("    '{")
                .append(String.format("\"soil_humidity\": %.2f,", data.soilHumidity()))
                .append(String.format("\"air_temperature\": %.2f,", data.airTemperature()))
                .append(String.format("\"air_humidity\": %.2f", data.airHumidity()))
                .append("}',\n")
                .append(String.format("    %.2f,\n", data.waterTemperature()))
                .append(String.format("    '%s',\n", data.temperatureUnit()))
                .append(String.format("    %.2f,\n", data.waterVolume()))
                .append(String.format("    '%s',\n", data.volumeUnit()))
                .append(String.format("    '%s',\n", data.measurementMethod()))
                .append("    ").append(data.measurementDetails()).append(",\n")
                .append(String.format("    %.0f\n", data.irrigationLevel()))
                .append(");");

        return sql.toString();
    }

    public String generateNoSQLScript(ExportData data) {
        StringBuilder nosql = new StringBuilder();

        nosql.append("db.agricultural_records.insertOne({\n")
                .append(String.format("    timestamp: ISODate(\"%s\"),\n",
                        LocalDateTime.now().format(DATE_TIME_FORMATTER)))
                .append(String.format("    crop_type: \"%s\",\n", data.cropType()))
                .append("    sensor_data: {\n")
                .append(String.format("        soil_humidity: %.2f,\n", data.soilHumidity()))
                .append(String.format("        air_temperature: %.2f,\n", data.airTemperature()))
                .append(String.format("        air_humidity: %.2f\n", data.airHumidity()))
                .append("    },\n")
                .append("    water_data: {\n")
                .append(String.format("        temperature: %.2f,\n", data.waterTemperature()))
                .append(String.format("        temperature_unit: \"%s\",\n", data.temperatureUnit()))
                .append(String.format("        volume: %.2f,\n", data.waterVolume()))
                .append(String.format("        volume_unit: \"%s\"\n", data.volumeUnit()))
                .append("    },\n")
                .append("    measurement: {\n")
                .append(String.format("        method: \"%s\",\n", data.measurementMethod()))
                .append("        details: ").append(data.measurementDetails()).append("\n")
                .append("    },\n")
                .append(String.format("    irrigation_level: %.0f,\n", data.irrigationLevel()))
                .append("    created_at: new Date()\n")
                .append("});");

        return nosql.toString();
    }

    public String generateJSONData(ExportData data) {
        StringBuilder json = new StringBuilder();

        json.append("{\n")
                .append("    \"agricultural_record\": {\n")
                .append(String.format("        \"timestamp\": \"%s\",\n",
                        LocalDateTime.now().format(DATE_TIME_FORMATTER)))
                .append(String.format("        \"crop_type\": \"%s\",\n", data.cropType()))
                .append("        \"sensor_data\": {\n")
                .append(String.format("            \"soil_humidity\": %.2f,\n", data.soilHumidity()))
                .append(String.format("            \"air_temperature\": %.2f,\n", data.airTemperature()))
                .append(String.format("            \"air_humidity\": %.2f\n", data.airHumidity()))
                .append("        },\n")
                .append("        \"water_data\": {\n")
                .append(String.format("            \"temperature\": %.2f,\n", data.waterTemperature()))
                .append(String.format("            \"temperature_unit\": \"%s\",\n", data.temperatureUnit()))
                .append(String.format("            \"volume\": %.2f,\n", data.waterVolume()))
                .append(String.format("            \"volume_unit\": \"%s\"\n", data.volumeUnit()))
                .append("        },\n")
                .append("        \"measurement\": {\n")
                .append(String.format("            \"method\": \"%s\",\n", data.measurementMethod()))
                .append("            \"details\": ").append(data.measurementDetails())
                .append("\n        },\n")
                .append(String.format("        \"irrigation_level\": %.0f\n", data.irrigationLevel()))
                .append("    }\n")
                .append("}");

        return json.toString();
    }
}