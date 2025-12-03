package com.example.fitnesstracker.dto.request.workout;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddExerciseToWorkoutDTO {

    @NotNull(message = "ID del ejercicio es requerido")
    private Long exerciseId;

    @NotNull(message = "Número de series es requerido")
    @Positive(message = "Series debe ser positivo")
    private Integer sets;

    @NotNull(message = "Número de repeticiones es requerido")
    @Positive(message = "Repeticiones debe ser positivo")
    private Integer reps;

    @Positive(message = "Peso debe ser positivo")
    private Double weight;

    @Positive(message = "Descanso debe ser positivo")
    private Integer restSeconds;

    @NotNull(message = "Orden en el workout es requerido")
    @Positive(message = "Orden debe ser positivo")
    private Integer orderInWorkout;

    @Size(max = 500, message = "Notas no pueden exceder 500 caracteres")
    private String notes;
}