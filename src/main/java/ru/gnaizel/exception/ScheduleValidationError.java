package ru.gnaizel.exception;

public class ScheduleValidationError extends RuntimeException {
    public ScheduleValidationError(String message) {
        super(message);
    }
}
