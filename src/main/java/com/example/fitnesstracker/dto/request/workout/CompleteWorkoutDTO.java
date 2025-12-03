package com.example.fitnesstracker.dto.request.workout;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteWorkoutDTO {

    @Min(value = 1, message = "Rating mínimo es 1")
    @Max(value = 5, message = "Rating máximo es 5")
    private Integer rating;

    @Size(max = 1000, message = "Notas no pueden exceder 1000 caracteres")
    private String notes;

    private List<ExerciseLogDTO> exerciseLogs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExerciseLogDTO {
        @NotNull(message = "ID del ejercicio del workout es requerido")
        private Long workoutExerciseId;

        @NotNull(message = "Series completadas es requerido")
        @Positive(message = "Series completadas debe ser positivo")
        private Integer setsCompleted;

        @NotNull(message = "Repeticiones completadas es requerido")
        @Positive(message = "Repeticiones completadas debe ser positivo")
        private Integer repsCompleted;

        @Positive(message = "Peso usado debe ser positivo")
        private Double weightUsed;

        @Size(max = 500, message = "Notas no pueden exceder 500 caracteres")
        private String notes;
    }
}