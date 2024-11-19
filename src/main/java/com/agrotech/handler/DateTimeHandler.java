package com.agrotech.handler;

import com.agrotech.service.DateTimeService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalDateTime;

public class DateTimeHandler {
    private final DateTimeService dateTimeService;
    private final Label dateLabel;
    private final Label lastUpdateLabel;
    private Timeline clockTimeline;

    public DateTimeHandler(Label dateLabel, Label lastUpdateLabel) {
        this.dateTimeService = new DateTimeService();
        this.dateLabel = dateLabel;
        this.lastUpdateLabel = lastUpdateLabel;
        setupClock();
    }

    private void setupClock() {
        clockTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateDateTime())
        );
        clockTimeline.setCycleCount(Animation.INDEFINITE);
    }

    public void startClock() {
        clockTimeline.play();
    }

    private void updateDateTime() {
        dateLabel.setText(dateTimeService.getFormattedCurrentDate());
    }

    public void updateLastUpdateTime() {
        lastUpdateLabel.setText(dateTimeService.formatTime());
    }

    public void updateLastUpdateTime(LocalDateTime timestamp) {
        if (timestamp != null) {
            lastUpdateLabel.setText(dateTimeService.formatTime(timestamp));
        } else {
            updateLastUpdateTime(); // Usa la hora actual si el timestamp es null
        }
    }
}
