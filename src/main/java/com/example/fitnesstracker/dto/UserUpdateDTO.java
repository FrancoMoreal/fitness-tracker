package com.example.fitnesstracker.dto;

import com.example.fitnesstracker.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para actualizar un usuario existente (todos los campos son opcionales)")
public class UserUpdateDTO {

    @Schema(
            description = "Nuevo nombre de usuario (debe ser único)",
            example = "john_doe_updated"
    )
    private String username;

    @Schema(
            description = "Nuevo correo electrónico (debe ser único)",
            example = "newemail@example.com"
    )
    private String email;

    @Schema(
            description = "Nueva contraseña (será encriptada)",
            example = "newSecurePassword123",
            format = "password"
    )
    private String password;

    @Schema(
            description = "Habilitar o deshabilitar el usuario",
            example = "true"
    )
    private Boolean enabled;

    @Schema(
            description = "Nuevo rol del usuario (solo admins deberían poder cambiar esto)",
            example = "ADMIN"
    )
    private UserRole role;
}