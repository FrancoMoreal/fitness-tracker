package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.response.UserDTO;
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

    //Registro de usuario con validaciones
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
        user.setRole(UserRole.MEMBER); // Asignar rol por defecto
        user.setEnable(true); // Asegurar que está habilitado
        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    //Login de usuario
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

        // Si esta bien, retorna el usuario.
        return userMapper.toDto(user);
    }

    //Obtiene todos los usuarios activos (no eliminados)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllActive().stream().map(userMapper::toDto).toList();
    }

    // Obtiene un usuario por ID (solo si está activo)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findByIdActive(id).orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toDto(user);
    }

    // Obtiene un usuario por external ID (UUID)
    public UserDTO getUserByExternalId(String externalId) {
        User user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new UserNotFoundException("No se encontró usuario con external ID: " + externalId));

        return userMapper.toDto(user);
    }

    // Elimina un usuario LÓGICAMENTE (soft delete)
    public void deleteUser(Long id) {
        User user = userRepository.findByIdActive(id).orElseThrow(() -> new UserNotFoundException(id));

        user.softDelete();
        userRepository.save(user);
    }

    // Restaura un usuario eliminado lógicamente
    public UserDTO restoreUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (user.isActive()) {
            throw new InvalidUserDataException("El usuario no está eliminado");
        }

        user.restore();
        User restoredUser = userRepository.save(user);
        return userMapper.toDto(restoredUser);
    }

    //Elimina un usuario PERMANENTEMENTE de la base de datos
    public void permanentlyDeleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
    }

    //Actualiza un usuario existente
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        validateUsernameUpdate(existingUser, userUpdateDTO);
        validateEmailUpdate(existingUser, userUpdateDTO);
        validatePasswordUpdate(userUpdateDTO);

        updateFields(existingUser, userUpdateDTO);

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    private void validateUsernameUpdate(User existingUser, UserUpdateDTO dto) {
        String newUsername = dto.getUsername();
        if (newUsername != null && !newUsername.trim().isEmpty()
                && !existingUser.getUsername().equals(newUsername)
                && userRepository.findByUsername(newUsername).isPresent()) {
            throw new UserAlreadyExistsException("username", newUsername);
        }
    }

    private void validateEmailUpdate(User existingUser, UserUpdateDTO dto) {
        String newEmail = dto.getEmail();
        if (newEmail != null && !newEmail.trim().isEmpty()
                && !existingUser.getEmail().equals(newEmail)
                && userRepository.existsByEmail(newEmail)) {
            throw new UserAlreadyExistsException("email", newEmail);
        }
    }

    private void validatePasswordUpdate(UserUpdateDTO dto) {
        String newPassword = dto.getPassword();
        if (newPassword != null && !newPassword.isBlank() && newPassword.length() < 6) {
            throw new InvalidUserDataException("password", "La contraseña debe tener al menos 6 caracteres");
        }
    }

    private void updateFields(User user, UserUpdateDTO dto) {
        if (dto.getUsername() != null && !dto.getUsername().trim().isEmpty()) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getEnable() != null) {
            user.setEnable(dto.getEnable());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
    }
    /**
     * Obtiene todos los usuarios con un rol específico
     */
    public List<UserDTO> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream().map(userMapper::toDto).toList();
    }
}