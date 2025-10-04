package com.example.fitnesstracker.enums;

public enum UserRole {
    USER("Usuario"),
    ADMIN("Administrador"),
    TRAINER("Entrenador");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Método útil para obtener el rol por su nombre
     * Lanza IllegalArgumentException si el rol no existe
     */
    public static UserRole fromString(String role) {
        if (role == null) {
            return USER; // Valor por defecto
        }

        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol inválido: " + role + ". Los roles válidos son: USER, ADMIN, TRAINER");
        }
    }
}