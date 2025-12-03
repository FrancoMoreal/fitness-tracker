package com.example.fitnesstracker.enums;

public enum WorkoutPlanStatus {
    DRAFT("Borrador"),
    ACTIVE("Activo"),
    COMPLETED("Completado"),
    PAUSED("Pausado");

    private final String displayName;

    WorkoutPlanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}