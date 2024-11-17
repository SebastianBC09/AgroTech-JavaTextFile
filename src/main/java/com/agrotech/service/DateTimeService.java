package com.agrotech.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public String formatDateTime() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    public String formatTime() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }

    public String formatCustomDateTime(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public String getFormattedCurrentDate() {
        return "Fecha: " + formatDateTime();
    }
}
