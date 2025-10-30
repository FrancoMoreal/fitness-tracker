package com.example.fitnesstracker.controller;

import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios (sin endpoints de autenticación).
 * La autenticación y el registro están ahora centralizados en `AuthController`.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "Gestión de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios", description = "Obtener todos los usuarios activos (solo ADMIN)")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("GET /api/users - Listando usuarios");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene un usuario por su ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - Obteniendo usuario", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/external/{externalId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Obtener usuario por External ID", description = "Obtiene un usuario por su externalId")
    public ResponseEntity<UserDTO> getUserByExternalId(@PathVariable String externalId) {
        log.info("GET /api/users/external/{} - Obteniendo usuario", externalId);
        UserDTO user = userService.getUserByExternalId(externalId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuarios por rol", description = "Lista usuarios filtrados por rol (solo ADMIN)")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable UserRole role) {
        log.info("GET /api/users/role/{} - Obteniendo usuarios por rol", role);
        List<UserDTO> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Actualizar usuario", description = "Actualiza datos de un usuario")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("PUT /api/users/{} - Actualizando usuario", id);
        UserDTO updated = userService.updateUser(id, userUpdateDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario (soft delete)", description = "Marca un usuario como eliminado (solo ADMIN)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - Eliminando usuario", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restaurar usuario", description = "Restaura un usuario eliminado (solo ADMIN)")
    public ResponseEntity<UserDTO> restoreUser(@PathVariable Long id) {
        log.info("POST /api/users/{}/restore - Restaurando usuario", id);
        UserDTO restored = userService.restoreUser(id);
        return ResponseEntity.ok(restored);
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario permanentemente", description = "Borra un usuario de la base de datos (solo ADMIN)")
    public ResponseEntity<Void> permanentlyDeleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{}/permanent - Eliminando usuario permanentemente", id);
        userService.permanentlyDeleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
