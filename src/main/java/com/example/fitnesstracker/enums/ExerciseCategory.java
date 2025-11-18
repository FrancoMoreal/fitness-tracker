package com.example.fitnesstracker.enums;

public enum ExerciseCategory {
    CARDIO("Cardio"),
    STRENGTH("Fuerza"),
    FLEXIBILITY("Flexibilidad"),
    BALANCE("Equilibrio"),
    SPORTS("Deportes");

    private final String displayName;

    ExerciseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}