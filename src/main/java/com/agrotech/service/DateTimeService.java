package com.agrotech.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeService {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public String getFormattedCurrentDate() {
        return LocalDateTime.now().format(dateTimeFormatter);
    }

    public String formatTime() {
        return "Última actualización: " + LocalDateTime.now().format(timeFormatter);
    }

    public String formatTime(LocalDateTime timestamp) {
        return "Última actualización: " + timestamp.format(timeFormatter);
    }

    public String formatDateTime() {
        return LocalDateTime.now().format(dateTimeFormatter);
    }
}
