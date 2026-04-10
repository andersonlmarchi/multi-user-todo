package br.com.jtech.tasklist.application.core.exceptions;

public class ApiConflictException extends RuntimeException {
    public ApiConflictException(String message) {
        super(message);
    }
}
