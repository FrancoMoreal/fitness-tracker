package com.example.fitnesstracker.controller;

import com.example.fitnesstracker.dto.UserDTO;
import com.example.fitnesstracker.dto.UserLoginDTO;
import com.example.fitnesstracker.dto.UserRegisterDTO;
import com.example.fitnesstracker.dto.UserUpdateDTO;
import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * POST /api/users/register
     * Registra un nuevo usuario
     *
     * Posibles respuestas:
     * - 201 Created: Usuario creado exitosamente
     * - 400 Bad Request: Datos inválidos
     * - 409 Conflict: Usuario ya existe
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        UserDTO registeredUser = userService.registerUser(userRegisterDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    /**
     * POST /api/users/login
     * Autentica un usuario
     *
     * Posibles respuestas:
     * - 200 OK: Login exitoso, retorna datos del usuario
     * - 400 Bad Request: Credenciales inválidas
     * - 404 Not Found: Usuario no existe
     */
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody UserLoginDTO userLoginDTO) {
        UserDTO user = userService.login(userLoginDTO.getUsername(), userLoginDTO.getPassword());
        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/users
     * Obtiene todos los usuarios registrados
     *
     * Posibles respuestas:
     * - 200 OK: Lista de usuarios
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/{id}
     * Obtiene un usuario por su ID
     *
     * Posibles respuestas:
     * - 200 OK: Usuario encontrado
     * - 404 Not Found: Usuario no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }



    /**
     * PUT /api/users/{id}
     * Actualiza un usuario existente
     *
     * Posibles respuestas:
     * - 200 OK: Usuario actualizado exitosamente
     * - 400 Bad Request: Datos inválidos
     * - 404 Not Found: Usuario no existe
     * - 409 Conflict: Username o email ya están en uso
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateDTO userUpdateDTO) {
        UserDTO updatedUser = userService.updateUser(id, userUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * DELETE /api/users/{id}
     * Elimina un usuario
     *
     * Posibles respuestas:
     * - 204 No Content: Usuario eliminado exitosamente
     * - 404 Not Found: Usuario no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}