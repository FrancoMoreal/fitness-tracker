package com.example.fitnesstracker.enums;


public enum AssignmentStatus {
    NO_TRAINER("Sin entrenador asignado"),
    PENDING("Esperando respuesta del entrenador"),
    ACTIVE("Entrenador asignado y activo"),
    REJECTED("Solicitud rechazada por el entrenador"),
    CANCELLED("Solicitud cancelada por el miembro");

    private final String displayName;

    AssignmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
