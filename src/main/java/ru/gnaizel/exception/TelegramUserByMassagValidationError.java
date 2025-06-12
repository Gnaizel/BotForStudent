package ru.gnaizel.exception;

public class TelegramUserByMassagValidationError extends RuntimeException {
    public TelegramUserByMassagValidationError(String message) {
        super(message);
    }
}
