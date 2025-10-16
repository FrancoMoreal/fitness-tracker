package com.example.fitnesstracker.dto.request.member;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterMemberDTO {

    @NotBlank(message = "Username es requerido")
    @Size(min = 3, max = 50, message = "Username debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username solo puede contener letras, números, guión y guión bajo")
    private String username;

    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe ser válido")
    private String email;

    @NotBlank(message = "Password es requerido")
    @Size(min = 8, max = 100, message = "Password debe tener entre 8 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "Password debe contener mayúscula, minúscula, número y carácter especial")
    private String password;

    @NotBlank(message = "Nombre es requerido")
    @Size(min = 2, max = 50, message = "Nombre debe tener entre 2 y 50 caracteres")
    private String firstName;

    @NotBlank(message = "Apellido es requerido")
    @Size(min = 2, max = 50, message = "Apellido debe tener entre 2 y 50 caracteres")
    private String lastName;

    @NotBlank(message = "Teléfono es requerido")
    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{9,}$", message = "Teléfono debe ser válido")
    private String phone;

    @NotNull(message = "Fecha de nacimiento es requerida")
    @Past(message = "Fecha de nacimiento debe ser en el pasado")
    private LocalDate dateOfBirth;


    @NotBlank(message = "Tipo de membresía es requerido")
    @Size(max = 50, message = "Tipo de membresía no puede exceder 50 caracteres")
    private String membershipType;
}
