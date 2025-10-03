package com.example.fitnesstracker.exception;

public class InvalidUserDataException extends RuntimeException {

    public InvalidUserDataException(String message) {
        super(message);
    }

    public InvalidUserDataException(String field, String reason) {
        super("Dato inválido en el campo '" + field + "': " + reason);
    }
}