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
@Schema(description = "Datos del usuario (sin contraseña por seguridad)")
public class UserDTO {

    @Schema(description = "ID único del usuario", example = "1")
    private Long id;

    @Schema(description = "Nombre de usuario", example = "john_doe")
    private String username;

    @Schema(description = "Correo electrónico", example = "john@example.com")
    private String email;

    @Schema(description = "Estado del usuario (activo/inactivo)", example = "true")
    private Boolean enabled;

    @Schema(description = "Rol del usuario en el sistema", example = "USER")
    private UserRole role;
}