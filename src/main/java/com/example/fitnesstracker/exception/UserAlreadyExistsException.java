package com.example.fitnesstracker.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String email) {
        super("Ya existe un usuario registrado con el email: " + email);
    }

    public UserAlreadyExistsException(String field, String value) {
        super("Ya existe un usuario con " + field + ": " + value);
    }
}