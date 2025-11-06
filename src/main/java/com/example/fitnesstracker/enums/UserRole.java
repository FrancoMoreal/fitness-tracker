package com.example.fitnesstracker.enums;

public enum UserRole {
    ADMIN("Administrador del sistema"),
    USER("Usuario regular");


    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isUser() {
        return this == USER;
    }
}
