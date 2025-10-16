package com.example.fitnesstracker.dto.request.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class UpdateMemberDTO {

    @NotBlank(message = "Nombre es requerido")
    @Size(min = 2, max = 50, message = "Nombre debe tener entre 2 y 50 caracteres")
    private String firstName;

    @NotBlank(message = "Apellido es requerido")
    @Size(min = 2, max = 50, message = "Apellido debe tener entre 2 y 50 caracteres")
    private String lastName;

    @NotBlank(message = "Teléfono es requerido")
    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{9,}$", message = "Teléfono debe ser válido")
    private String phone;

    @Positive(message = "Altura debe ser positiva")
    private Double height;

    @Positive(message = "Peso debe ser positivo")
    private Double weight;
}
