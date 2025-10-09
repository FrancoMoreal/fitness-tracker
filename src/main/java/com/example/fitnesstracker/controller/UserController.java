package com.example.fitnesstracker.controller;

import com.example.fitnesstracker.api.UserApi;
import com.example.fitnesstracker.dto.request.UserLoginDTO;
import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios.
 * Implementa la interfaz UserApi que contiene toda la documentación de Swagger.
 *
 * Este controlador se mantiene limpio y enfocado solo en la lógica,
 * delegando la documentación a la interfaz UserApi.
 */
@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserDTO> register(UserRegisterDTO userRegisterDTO) {
        UserDTO registeredUser = userService.registerUser(userRegisterDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @Override
    public ResponseEntity<AuthResponse> login(UserLoginDTO userLoginDTO) {
        AuthResponse authResponse = userService.login(userLoginDTO.getUsername(), userLoginDTO.getPassword());
        return ResponseEntity.ok(authResponse);
    }


    @Override
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Override
    public ResponseEntity<UserDTO> getUserById(Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<UserDTO> getUserByExternalId(String externalId) {
        UserDTO user = userService.getUserByExternalId(externalId);
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<List<UserDTO>> getUsersByRole(UserRole role) {
        List<UserDTO> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @Override
    public ResponseEntity<UserDTO> updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        UserDTO updatedUser = userService.updateUser(id, userUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserDTO> restoreUser(Long id) {
        UserDTO restoredUser = userService.restoreUser(id);
        return ResponseEntity.ok(restoredUser);
    }

    @Override
    public ResponseEntity<Void> permanentlyDeleteUser(Long id) {
        userService.permanentlyDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

}