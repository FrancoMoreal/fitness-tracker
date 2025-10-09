package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Registro de usuario con validaciones
     * Lanza UserAlreadyExistsException si el username o email ya existen
     */
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

    /**
     * Autentica un usuario y genera un token JWT.
     *
     * <p>Verifica que el username existe y que la contraseña coincide.
     * Si las credenciales son válidas, genera un token JWT.
     *
     * @param username Nombre de usuario
     * @param rawPassword Contraseña en texto plano
     * @return AuthResponse con el token JWT y datos del usuario
     * @throws InvalidUserDataException si username o password están vacíos
     * @throws UserNotFoundException si el username no existe o las credenciales son inválidas
     */
    public AuthResponse login(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidUserDataException("username", "El nombre de usuario es obligatorio");
        }

        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new InvalidUserDataException("password", "La contraseña es obligatoria");
        }

        // Autenticar con Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, rawPassword)
        );

        // Establecer autenticación en el contexto
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generar token JWT - usar username directamente
        String token = jwtTokenProvider.generateToken(username);

        // Obtener datos del usuario
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("No se encontró usuario con username: " + username));

        UserDTO userDTO = userMapper.toDto(user);

        return new AuthResponse(token, userDTO);
    }


    /**
     * Obtiene todos los usuarios activos (no eliminados)
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllActive()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    /**
     * Obtiene un usuario por ID (solo si está activo)
     * Lanza UserNotFoundException si no existe o está eliminado
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findByIdActive(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toDto(user);
    }

    /**
     * Obtiene un usuario por external ID (UUID)
     * Lanza UserNotFoundException si no existe o está eliminado
     */
    public UserDTO getUserByExternalId(String externalId) {
        User user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new UserNotFoundException("No se encontró usuario con external ID: " + externalId));

        return userMapper.toDto(user);
    }

    /**
     * Elimina un usuario LÓGICAMENTE (soft delete)
     * No elimina físicamente de la base de datos
     * Lanza UserNotFoundException si no existe
     */
    public void deleteUser(Long id) {
        User user = userRepository.findByIdActive(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.softDelete();
        userRepository.save(user);
    }

    /**
     * Restaura un usuario eliminado lógicamente
     * Lanza UserNotFoundException si no existe
     */
    public UserDTO restoreUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (user.isActive()) {
            throw new InvalidUserDataException("El usuario no está eliminado");
        }

        user.restore();
        User restoredUser = userRepository.save(user);
        return userMapper.toDto(restoredUser);
    }

    /**
     * Elimina un usuario PERMANENTEMENTE de la base de datos
     * ⚠️ USAR CON PRECAUCIÓN - Esta acción no se puede deshacer
     * Solo debe usarse en casos excepcionales o por requerimientos legales
     */
    public void permanentlyDeleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
    }

    /**
     * Actualiza un usuario existente
     * Lanza UserNotFoundException si no existe
     * Lanza UserAlreadyExistsException si el nuevo username/email ya están en uso
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

        if (userUpdateDTO.getEnable() != null) {
            existingUser.setEnable(userUpdateDTO.getEnable());
        }

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
     * Obtiene todos los usuarios con un rol específico
     */
    public List<UserDTO> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }
}