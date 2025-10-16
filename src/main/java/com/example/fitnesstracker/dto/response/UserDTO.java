package com.example.fitnesstracker.dto.response;

import com.example.fitnesstracker.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Datos del usuario (sin contraseña por seguridad)")
public class UserDTO {

    @Schema(description = "ID único del usuario", example = "1")
    private Long id;

    @Schema(description = "Identificador externo del usuario (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String externalId;

    @Schema(description = "Nombre de usuario", example = "john_doe")
    private String username;

    @Schema(description = "Correo electrónico", example = "john@example.com")
    private String email;

    @Schema(description = "Estado del usuario (activo/inactivo)", example = "true")
    private Boolean enabled;

    @Schema(description = "Rol del usuario en el sistema", example = "USER")
    private UserRole role;

    @Schema(description = "Fecha de creación", example = "2024-01-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de última actualización", example = "2024-01-02T15:30:00")
    private LocalDateTime updatedAt;
}
