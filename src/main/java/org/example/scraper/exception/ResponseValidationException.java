package org.example.scraper.exception;

public class ResponseValidationException extends RuntimeException {
    public ResponseValidationException(String message) {
        super(message);
    }
}
