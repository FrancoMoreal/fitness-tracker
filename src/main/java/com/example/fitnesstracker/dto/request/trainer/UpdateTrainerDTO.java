package com.example.fitnesstracker.dto.request.trainer;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTrainerDTO {

    @NotBlank(message = "Nombre es requerido")
    @Size(min = 2, max = 50, message = "Nombre debe tener entre 2 y 50 caracteres")
    private String firstName;

    @NotBlank(message = "Apellido es requerido")
    @Size(min = 2, max = 50, message = "Apellido debe tener entre 2 y 50 caracteres")
    private String lastName;

    @NotBlank(message = "Especialidad es requerida")
    @Size(min = 3, max = 100, message = "Especialidad debe tener entre 3 y 100 caracteres")
    private String specialty;

    @NotNull(message = "Tarifa horaria es requerida")
    @Positive(message = "Tarifa horaria debe ser positiva")
    @DecimalMin(value = "0.01", message = "Tarifa horaria debe ser mayor a 0")
    private BigDecimal hourlyRate;

    private Boolean isActive;
}