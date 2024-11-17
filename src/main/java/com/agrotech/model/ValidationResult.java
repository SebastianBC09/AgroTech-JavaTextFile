package com.agrotech.model;

public class ValidationResult {
    private final boolean isValid;
    private final String message;

    private ValidationResult(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult error(String message) {
        return new ValidationResult(false, message);
    }

    public boolean isValid() {
        return isValid;
    }

    public String getMessage() {
        return message;
    }
}
