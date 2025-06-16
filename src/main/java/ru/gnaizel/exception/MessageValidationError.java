package ru.gnaizel.exception;

public class MessageValidationError extends RuntimeException {
    public MessageValidationError(String message) {
        super(message);
    }
}
