package ru.gnaizel.exception;

public class TelegramUpdateValidationError extends RuntimeException {
    public TelegramUpdateValidationError(String message) {
        super(message);
    }
}
