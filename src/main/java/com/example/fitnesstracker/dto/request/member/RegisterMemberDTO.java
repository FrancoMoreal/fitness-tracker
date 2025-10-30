// java
package com.example.fitnesstracker.dto.request.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(example = "jdoe", description = "Nombre de usuario")
    private String username;

    @NotBlank
    @Email
    @Schema(example = "jdoe@example.com", description = "Email del usuario")
    private String email;

    @NotBlank
    @Size(min = 6)
    @Schema(example = "secret123", description = "Contraseña")
    private String password;

    @NotBlank
    @Schema(example = "Juan", description = "Nombre")
    private String firstName;

    @NotBlank
    @Schema(example = "Pérez", description = "Apellido")
    private String lastName;

    @NotBlank
    @Schema(example = "+34123456789", description = "Teléfono en formato internacional")
    private String phone;

    @Schema(example = "1990-05-20", description = "Fecha de nacimiento (YYYY-MM-DD)")
    private LocalDate dateOfBirth;


}
