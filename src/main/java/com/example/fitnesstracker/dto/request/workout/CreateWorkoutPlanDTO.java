package com.example.fitnesstracker.dto.request.workout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWorkoutPlanDTO {

    @NotBlank(message = "Nombre del plan es requerido")
    @Size(min = 3, max = 100, message = "Nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @Size(max = 1000, message = "Descripción no puede exceder 1000 caracteres")
    private String description;

    @NotNull(message = "ID del miembro es requerido")
    private Long memberId;

    @NotNull(message = "Fecha de inicio es requerida")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 1000, message = "Notas no pueden exceder 1000 caracteres")
    private String notes;
}