package com.example.fitnesstracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Credenciales para iniciar sesión")
public class UserLoginDTO {

    @Schema(description = "Nombre de usuario", example = "john_doe")
    private String username;

    @Schema(description = "Contraseña del usuario", example = "securePassword123", format = "password")
    private String password;
}