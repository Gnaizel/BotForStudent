package ru.gnaizel.exception;

public class UserValidationError extends RuntimeException {
    public UserValidationError(String message) {
        super(message);
    }
}
