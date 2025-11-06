package com.example.fitnesstracker.dto.request.assignment;

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
public class RequestTrainerDTO {

    @NotNull(message = "ID del entrenador es requerido")
    private Long trainerId;

    @Size(max = 500, message = "El mensaje no puede exceder 500 caracteres")
    private String message;
}