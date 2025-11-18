package com.example.fitnesstracker.dto.request.exercise;

import com.example.fitnesstracker.enums.DifficultyLevel;
import com.example.fitnesstracker.enums.ExerciseCategory;
import com.example.fitnesstracker.enums.MuscleGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateExerciseDTO {

    @NotBlank(message = "Nombre del ejercicio es requerido")
    @Size(min = 3, max = 100, message = "Nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @Size(max = 1000, message = "Descripción no puede exceder 1000 caracteres")
    private String description;

    @NotNull(message = "Categoría es requerida")
    private ExerciseCategory category;

    @NotNull(message = "Grupo muscular es requerido")
    private MuscleGroup primaryMuscle;

    @Size(max = 500, message = "URL del video no puede exceder 500 caracteres")
    private String videoUrl;

    @Size(max = 500, message = "URL de la imagen no puede exceder 500 caracteres")
    private String imageUrl;

    @NotNull(message = "Nivel de dificultad es requerido")
    private DifficultyLevel difficulty;

    @Size(max = 2000, message = "Instrucciones no pueden exceder 2000 caracteres")
    private String instructions;

    @Size(max = 500, message = "Equipamiento no puede exceder 500 caracteres")
    private String equipment;
}