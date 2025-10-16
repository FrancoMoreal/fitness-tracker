// java
package com.example.fitnesstracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta de autenticación con token JWT y datos del usuario")
public class AuthResponse {

    @Schema(description = "Token JWT para autenticación", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Builder.Default
    @Schema(description = "Tipo de token", example = "Bearer", defaultValue = "Bearer")
    private String type = "Bearer";

    @Schema(description = "Información básica del usuario autenticado")
    private UserDTO user;

    @Schema(description = "Información del miembro si el usuario es un Member")
    private MemberDTO member;

    @Schema(description = "Información del entrenador si el usuario es un Trainer")
    private TrainerDTO trainer;

    @Schema(description = "Fecha y hora de expiración del token")
    private LocalDateTime expiresAt;

    @Schema(description = "Token de refresco para renovar el token principal")
    private String refreshToken;

    @Schema(description = "Mensaje adicional (opcional)")
    private String message;
}
