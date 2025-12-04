package com.example.fitnesstracker.dto.request.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload para registrar un miembro")
public class RegisterMemberDTO {

    @NotBlank(message = "Username es requerido")
    @Size(min = 3, max = 50, message = "Username debe tener entre 3 y 50 caracteres")
    @Schema(example = "jdoe", description = "Nombre de usuario")
    private String username;

    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe ser válido")
    @Schema(example = "jdoe@example.com", description = "Email del usuario")
    private String email;

    @NotBlank(message = "Password es requerido")
    @Size(min = 8, max = 100, message = "Password debe tener entre 8 y 100 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password debe contener mayúscula, minúscula, número y carácter especial"
    )
    @Schema(example = "SecurePass123!", description = "Contraseña")
    private String password;

    @NotBlank(message = "Nombre es requerido")
    @Size(min = 2, max = 50, message = "Nombre debe tener entre 2 y 50 caracteres")
    @Schema(example = "Juan", description = "Nombre")
    private String firstName;

    @NotBlank(message = "Apellido es requerido")
    @Size(min = 2, max = 50, message = "Apellido debe tener entre 2 y 50 caracteres")
    @Schema(example = "Pérez", description = "Apellido")
    private String lastName;

    @NotBlank(message = "Teléfono es requerido")
    @Schema(example = "+34123456789", description = "Teléfono en formato internacional")
    private String phone;

    @NotNull(message = "Fecha de nacimiento es requerida")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @Schema(example = "1990-05-20", description = "Fecha de nacimiento (YYYY-MM-DD)")
    private LocalDate dateOfBirth;
}