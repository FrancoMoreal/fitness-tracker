package com.example.fitnesstracker.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Usuario no encontrado con ID: " + id);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}