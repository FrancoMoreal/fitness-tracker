package com.example.fitnesstracker.dto.request.trainer;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterTrainerDTO {

    @NotBlank(message = "Username es requerido")
    @Size(min = 3, max = 50, message = "Username debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username solo puede contener letras, números, guión y guión bajo")
    private String username;

    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe ser válido")
    private String email;

    @NotBlank(message = "Password es requerido")
    @Size(min = 8, max = 100, message = "Password debe tener entre 8 y 100 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password debe contener mayúscula, minúscula, número y carácter especial"
    )
    private String password;

    @NotBlank(message = "Nombre es requerido")
    @Size(min = 2, max = 50, message = "Nombre debe tener entre 2 y 50 caracteres")
    private String firstName;

    @NotBlank(message = "Apellido es requerido")
    @Size(min = 2, max = 50, message = "Apellido debe tener entre 2 y 50 caracteres")
    private String lastName;

    @NotBlank(message = "Especialidad es requerida")
    @Size(min = 3, max = 100, message = "Especialidad debe tener entre 3 y 100 caracteres")
    private String specialty;

    @NotEmpty(message = "Al menos una certificación es requerida")
    @Size(min = 1, max = 10, message = "Máximo 10 certificaciones")
    private List<@NotBlank(message = "Certificación no puede estar vacía") String> certifications;

    @NotNull(message = "Tarifa horaria es requerida")
    @Positive(message = "Tarifa horaria debe ser positiva")
    @DecimalMin(value = "0.01", message = "Tarifa horaria debe ser mayor a 0")
    private BigDecimal hourlyRate;
}