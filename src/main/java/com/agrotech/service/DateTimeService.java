package com.agrotech.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeService {
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String LAST_UPDATE_TEMPLATE = "Última actualización: %s";

    public String getFormattedCurrentDate() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public String formatTime() {
        return String.format(LAST_UPDATE_TEMPLATE,
                LocalDateTime.now().format(TIME_FORMATTER));
    }

    public String formatTime(LocalDateTime timestamp) {
        return String.format(LAST_UPDATE_TEMPLATE,
                timestamp.format(TIME_FORMATTER));
    }
}
