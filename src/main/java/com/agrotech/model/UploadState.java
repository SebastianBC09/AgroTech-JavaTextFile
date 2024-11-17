package com.agrotech.model;

public enum UploadState {
    INITIAL(
            "-fx-background-color: #f8f9fa; -fx-border-color: #ced4da;",
            "Arrastre su archivo CSV aquí"
    ),
    DRAGGING(
            "-fx-background-color: #e9ecef; -fx-border-color: #0d6efd;",
            "Suelte el archivo para comenzar"
    ),
    PROCESSING(
            "-fx-background-color: #e9ecef; -fx-border-color: #0dcaf0;",
            "Procesando archivo..."
    ),
    SUCCESS(
            "-fx-background-color: #d1e7dd; -fx-border-color: #198754;",
            "Archivo procesado correctamente"
    ),
    ERROR(
            "-fx-background-color: #f8d7da; -fx-border-color: #dc3545;",
            "Error al procesar el archivo"
    );

    private final String style;
    private final String message;

    UploadState(String style, String message) {
        this.style = style;
        this.message = message;
    }

    public String getStyle() { return style; }
    public String getMessage() { return message; }
}
