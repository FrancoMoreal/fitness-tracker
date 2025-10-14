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
@Schema(description = "Datos necesarios para registrar un nuevo usuario")
public class UserRegisterDTO {

    @Schema(description = "Nombre de usuario único", example = "john_doe",  minLength = 3, maxLength = 50)
    private String username;

    @Schema(description = "Correo electrónico único del usuario", example = "john@example.com", format = "email")
    private String email;

    @Schema(description = "Contraseña del usuario (será encriptada)", example = "securePassword123", minLength = 6, format = "password")
    private String password;
}