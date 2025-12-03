package com.example.fitnesstracker.dto.request.workout;

import jakarta.validation.constraints.NotBlank;
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
public class AddWorkoutDayDTO {

    @NotBlank(message = "Nombre del día es requerido")
    @Size(min = 3, max = 100, message = "Nombre debe tener entre 3 y 100 caracteres")
    private String dayName;

    @NotNull(message = "Número de día es requerido")
    @Positive(message = "Número de día debe ser positivo")
    private Integer dayNumber;

    @Size(max = 1000, message = "Notas no pueden exceder 1000 caracteres")
    private String notes;
}