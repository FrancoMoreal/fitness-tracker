package com.example.fitnesstracker.api;

import com.example.fitnesstracker.dto.request.UserLoginDTO;
import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.dto.response.ErrorResponse;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API de gestión de usuarios.
 * Define el contrato de los endpoints relacionados con usuarios.
 *
 * Esta interfaz contiene toda la documentación de Swagger/OpenAPI,
 * manteniendo el controller limpio y enfocado en la lógica.
 */
@Tag(name = "Users", description = "API para gestión de usuarios del sistema")
@RequestMapping("/api/users")
public interface UserApi {

    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario en el sistema. El usuario se crea con rol USER por defecto y estado habilitado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class), examples = @ExampleObject(value = "{\"id\": 1, \"externalId\": \"550e8400-e29b-41d4-a716-446655440000\", \"username\": \"john_doe\", \"email\": \"john@example.com\", \"enabled\": true, \"role\": \"USER\", \"createdAt\": \"2025-10-04T10:30:00\", \"updatedAt\": \"2025-10-04T10:30:00\"}"))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (contraseña muy corta, email vacío, etc.)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El username o email ya están en uso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @PostMapping("/register")
    ResponseEntity<UserDTO> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del usuario a registrar", required = true, content = @Content(schema = @Schema(implementation = UserRegisterDTO.class), examples = @ExampleObject(value = "{\"username\": \"john_doe\", \"email\": \"john@example.com\", \"password\": \"securePassword123\"}")))
            @RequestBody UserRegisterDTO userRegisterDTO);

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario con su username y password. Retorna los datos del usuario si las credenciales son válidas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Credenciales inválidas (contraseña incorrecta)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @PostMapping("/login")
    ResponseEntity<UserDTO> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credenciales de acceso", required = true, content = @Content(schema = @Schema(implementation = UserLoginDTO.class), examples = @ExampleObject(value = "{\"username\": \"john_doe\", \"password\": \"securePassword123\"}")))
            @RequestBody UserLoginDTO userLoginDTO);

    @Operation(summary = "Obtener todos los usuarios activos", description = "Retorna una lista con todos los usuarios activos (no eliminados) registrados en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))) })
    @GetMapping
    ResponseEntity<List<UserDTO>> getAllUsers();

    @Operation(summary = "Obtener usuario por ID interno", description = "Busca y retorna un usuario específico según su ID interno de base de datos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado con el ID proporcionado o está eliminado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @GetMapping("/{id}")
    ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID interno del usuario", required = true, example = "1") @PathVariable Long id);

    @Operation(summary = "Obtener usuario por External ID (UUID)", description = "Busca un usuario usando su UUID público. Este es el método recomendado para APIs públicas ya que no expone IDs incrementales.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado con el External ID proporcionado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @GetMapping("/ext/{externalId}")
    ResponseEntity<UserDTO> getUserByExternalId(
            @Parameter(description = "External ID (UUID) del usuario", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String externalId);

    @Operation(summary = "Obtener usuarios por rol", description = "Retorna una lista de usuarios activos filtrados por su rol (USER, ADMIN, TRAINER)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios con el rol especificado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Rol inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @GetMapping("/role/{role}")
    ResponseEntity<List<UserDTO>> getUsersByRole(
            @Parameter(description = "Rol a filtrar (USER, ADMIN, TRAINER)", required = true, example = "ADMIN")
            @PathVariable UserRole role);

    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente. Solo se actualizan los campos enviados (campos null se ignoran)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El nuevo username o email ya están en uso por otro usuario", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @PutMapping("/{id}")
    ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID del usuario a actualizar", required = true, example = "1") @PathVariable
            Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos a actualizar del usuario", required = true, content = @Content(schema = @Schema(implementation = UserUpdateDTO.class), examples = @ExampleObject(value = "{\"email\": \"newemail@example.com\", \"role\": \"ADMIN\"}")))
            @RequestBody UserUpdateDTO userUpdateDTO);

    @Operation(summary = "Eliminar usuario (Soft Delete)", description = "Marca el usuario como eliminado sin borrarlo físicamente de la base de datos. El usuario puede ser restaurado posteriormente con el endpoint /restore.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente (sin contenido en la respuesta)"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID del usuario a eliminar", required = true, example = "1") @PathVariable
            Long id);

    @Operation(summary = "Restaurar usuario eliminado", description = "Restaura un usuario que fue eliminado lógicamente (soft delete), volviéndolo a marcar como activo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario restaurado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "El usuario no está eliminado (ya está activo)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @PostMapping("/{id}/restore")
    ResponseEntity<UserDTO> restoreUser(
            @Parameter(description = "ID del usuario a restaurar", required = true, example = "1") @PathVariable
            Long id);

    @Operation(summary = "Eliminar usuario permanentemente", description = "⚠️ PELIGRO: Elimina el usuario de forma permanente de la base de datos. Esta acción NO se puede deshacer. Use solo en casos excepcionales o por requerimientos legales (GDPR, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado permanentemente (sin contenido en la respuesta)"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @DeleteMapping("/{id}/permanent")
    ResponseEntity<Void> permanentlyDeleteUser(
            @Parameter(description = "ID del usuario a eliminar permanentemente", required = true, example = "1")
            @PathVariable Long id);
}