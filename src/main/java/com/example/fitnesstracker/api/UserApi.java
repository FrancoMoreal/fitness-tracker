package com.example.fitnesstracker.api;

import com.example.fitnesstracker.dto.request.UserLoginDTO;
import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.enums.UserRole;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@RequestMapping("/api/users")
public interface UserApi {

    @PostMapping("/register")
    ResponseEntity<UserDTO> register(@RequestBody UserRegisterDTO userRegisterDTO);

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@RequestBody UserLoginDTO userLoginDTO);

    @GetMapping
    ResponseEntity<List<UserDTO>> getAllUsers();

    @GetMapping("/{id}")
    ResponseEntity<UserDTO> getUserById(@PathVariable Long id);

    @GetMapping("/external/{externalId}")
    ResponseEntity<UserDTO> getUserByExternalId(@PathVariable String externalId);

    @GetMapping("/role/{role}")
    ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable UserRole role);

    @PutMapping("/{id}")
    ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO userUpdateDTO);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable Long id);

    @PatchMapping("/{id}/restore")
    ResponseEntity<UserDTO> restoreUser(@PathVariable Long id);

    @DeleteMapping("/{id}/permanent")
    ResponseEntity<Void> permanentlyDeleteUser(@PathVariable Long id);
}
