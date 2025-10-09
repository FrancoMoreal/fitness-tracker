package com.example.fitnesstracker.api;

import com.example.fitnesstracker.dto.request.UserLoginDTO;
import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.ErrorResponse;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@RequestMapping("/api/users")
public interface UserApi {

    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario en el sistema. El usuario se crea con rol MEMBER por defecto y estado habilitado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class), examples = @ExampleObject(value = "{\"id\": 1, \"externalId\": \"550e8400-e29b-41d4-a716-446655440000\", \"username\": \"john_doe\", \"email\": \"john@example.com\", \"enable\": true, \"role\": \"MEMBER\"}"))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (contraseña muy corta, email vacío, etc.)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El username o email ya están en uso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @PostMapping("/register")
    ResponseEntity<UserDTO> register(@RequestBody @Valid UserRegisterDTO userRegisterDTO);

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT junto con los datos del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(value = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"type\": \"Bearer\", \"user\": {\"id\": 1, \"externalId\": \"550e8400-e29b-41d4-a716-446655440000\", \"username\": \"john_doe\", \"email\": \"john@example.com\", \"enable\": true, \"role\": \"MEMBER\"}}"))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (username o password vacíos)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@RequestBody @Valid UserLoginDTO userLoginDTO);

    @Operation(summary = "Obtener todos los usuarios activos", description = "Retorna una lista con todos los usuarios activos (no eliminados) registrados en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))) })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    ResponseEntity<List<UserDTO>> getAllUsers();

    @Operation(summary = "Obtener usuario por ID", description = "Retorna un usuario específico según su ID interno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado o está eliminado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    ResponseEntity<UserDTO> getUserById(@PathVariable Long id);

    @Operation(summary = "Obtener usuario por UUID", description = "Retorna un usuario específico según su external ID (UUID)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/external/{externalId}")
    ResponseEntity<UserDTO> getUserByExternalId(@PathVariable String externalId);

    @Operation(summary = "Obtener usuarios por rol", description = "Retorna todos los usuarios que tienen un rol específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios filtrada por rol", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))) })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/role/{role}")
    ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable UserRole role);

    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El nuevo username o email ya están en uso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateDTO userUpdateDTO);

    @Operation(summary = "Eliminar usuario (soft delete)", description = "Marca un usuario como eliminado sin borrarlo físicamente de la base de datos")
    @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable Long id);

    @Operation(summary = "Restaurar usuario eliminado", description = "Restaura un usuario que fue eliminado lógicamente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario restaurado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "El usuario no está eliminado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{id}/restore")
    ResponseEntity<UserDTO> restoreUser(@PathVariable Long id);

    @Operation(summary = "Eliminar usuario permanentemente", description = "Elimina físicamente un usuario de la base de datos. Esta acción no se puede deshacer.")
    @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Usuario eliminado permanentemente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}/permanent")
    ResponseEntity<Void> permanentlyDeleteUser(@PathVariable Long id);
}
