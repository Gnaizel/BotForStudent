package org.example.scraper.exception;

public class ResponseValidationException extends Exception { // Фатальная для этого бота так что Exception не доебывайтес
    public ResponseValidationException(String message) {
        super(message);
    }
}
