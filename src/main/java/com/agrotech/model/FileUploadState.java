package com.agrotech.model;

public enum FileUploadState {
    INITIAL("message.dropzone.initial", "drop-zone-initial"),
    DRAGGING("message.dropzone.dragging", "drop-zone-dragging"),
    PROCESSING("message.dropzone.processing", "drop-zone-processing"),
    SUCCESS("message.dropzone.success", "drop-zone-success"),
    ERROR("message.dropzone.error", "drop-zone-error");

    private final String messageKey;
    private final String styleClass;

    FileUploadState(String messageKey, String styleClass) {
        this.messageKey = messageKey;
        this.styleClass = styleClass;
    }

    public String getMessageKey() { return messageKey; }
    public String getStyleClass() { return styleClass; }
}
