package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.UserAlreadyExistsException;
import com.example.fitnesstracker.exception.UserNotFoundException;
import com.example.fitnesstracker.mapper.UserMapper;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


     // Registro de usuario con validaciones

    public UserDTO registerUser(UserRegisterDTO userRegisterDTO) {
        // Validar que los campos requeridos no estén vacíos
        if (userRegisterDTO.getUsername() == null || userRegisterDTO.getUsername().trim().isEmpty()) {
            throw new InvalidUserDataException("username", "El nombre de usuario es obligatorio");
        }

        if (userRegisterDTO.getEmail() == null || userRegisterDTO.getEmail().trim().isEmpty()) {
            throw new InvalidUserDataException("email", "El email es obligatorio");
        }

        if (userRegisterDTO.getPassword() == null || userRegisterDTO.getPassword().trim().isEmpty()) {
            throw new InvalidUserDataException("password", "La contraseña es obligatoria");
        }

        // Validar longitud mínima de la contraseña
        if (userRegisterDTO.getPassword().length() < 6) {
            throw new InvalidUserDataException("password", "La contraseña debe tener al menos 6 caracteres");
        }

        // Verificar si ya existe un usuario con ese username
        if (userRepository.findByUsername(userRegisterDTO.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("username", userRegisterDTO.getUsername());
        }

        // Verificar si ya existe un usuario con ese email
        if (userRepository.existsByEmail(userRegisterDTO.getEmail())) {
            throw new UserAlreadyExistsException("email", userRegisterDTO.getEmail());
        }

        User user = userMapper.toEntity(userRegisterDTO);
        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }


    // Login de usuario

    public UserDTO login(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidUserDataException("username", "El nombre de usuario es obligatorio");
        }

        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new InvalidUserDataException("password", "La contraseña es obligatoria");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("No se encontró usuario con username: " + username));

        // Verificar si la contraseña coincide
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidUserDataException("Credenciales inválidas. Verifica tu usuario y contraseña.");
        }

        // Si todo está bien, retornar el usuario
        return userMapper.toDto(user);
    }

    /**
     * Obtiene todos los usuarios registrados
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    /**
     * Obtiene un usuario por ID
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toDto(user);
    }

    /**
     * Elimina un usuario por ID
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
    }

    /**
     * Actualiza un usuario existente
     */
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        // Verificar que el usuario existe
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Si cambió el username, verificar que no esté en uso por otro usuario
        if (userUpdateDTO.getUsername() != null &&
                !userUpdateDTO.getUsername().trim().isEmpty() &&
                !existingUser.getUsername().equals(userUpdateDTO.getUsername())) {

            if (userRepository.findByUsername(userUpdateDTO.getUsername()).isPresent()) {
                throw new UserAlreadyExistsException("username", userUpdateDTO.getUsername());
            }
        }

        // Si cambió el email, verificar que no esté en uso por otro usuario
        if (userUpdateDTO.getEmail() != null &&
                !userUpdateDTO.getEmail().trim().isEmpty() &&
                !existingUser.getEmail().equals(userUpdateDTO.getEmail())) {

            if (userRepository.existsByEmail(userUpdateDTO.getEmail())) {
                throw new UserAlreadyExistsException("email", userUpdateDTO.getEmail());
            }
        }

        // Validar longitud de contraseña si viene en el DTO
        if (userUpdateDTO.getPassword() != null &&
                !userUpdateDTO.getPassword().isBlank() &&
                userUpdateDTO.getPassword().length() < 6) {
            throw new InvalidUserDataException("password", "La contraseña debe tener al menos 6 caracteres");
        }

        // Actualizar campos manualmente para tener más control
        if (userUpdateDTO.getUsername() != null && !userUpdateDTO.getUsername().trim().isEmpty()) {
            existingUser.setUsername(userUpdateDTO.getUsername());
        }

        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().trim().isEmpty()) {
            existingUser.setEmail(userUpdateDTO.getEmail());
        }

//        if (userUpdateDTO.getEnabled() != null) {
//            existingUser.setEnabled(userUpdateDTO.getEnabled());
//        }

        if (userUpdateDTO.getRole() != null) {
            existingUser.setRole(userUpdateDTO.getRole());
        }

        // Encriptar password si viene en el DTO
        if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    /**
     * Obtiene  todos los usuarios con un rol específico
     */
    public List<UserDTO> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }
}