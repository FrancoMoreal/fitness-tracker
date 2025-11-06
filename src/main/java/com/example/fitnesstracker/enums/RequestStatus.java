package com.example.fitnesstracker.enums;

public enum RequestStatus {
    PENDING("Pendiente"),
    ACCEPTED("Aceptada"),
    REJECTED("Rechazada"),
    CANCELLED("Cancelada");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}