package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.enums.UserType;
import com.example.fitnesstracker.security.JwtTokenProvider;
import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.UserAlreadyExistsException;
import com.example.fitnesstracker.exception.UserNotFoundException;
import com.example.fitnesstracker.mapper.UserMapper;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    private static final String USERNAME_ALREADY_EXISTS = "El nombre de usuario ya está registrado";
    private static final String EMAIL_ALREADY_EXISTS = "El email ya está registrado";
    private static final String INVALID_PASSWORD = "La contraseña debe tener al menos 6 caracteres";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserDTO registerUser(UserRegisterDTO userRegisterDTO) {
        log.info("Registrando nuevo usuario: {}", userRegisterDTO.getUsername());
        validateUserRegistration(userRegisterDTO);

        User user = userMapper.toEntity(userRegisterDTO);
        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        user.setRole(UserRole.USER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("Usuario registrado exitosamente: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    public AuthResponse login(String username, String rawPassword) {
        log.info("Iniciando sesión para el usuario: {}", username);
        validateLoginCredentials(username, rawPassword);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, rawPassword)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        log.info("Inicio de sesión exitoso para el usuario: {}", username);
        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toDto(user))
                .build();
    }

    @Transactional
    public User createUserWithType(String username, String email, String password, UserType userType) {
        log.debug("Creando usuario con tipo: {}", userType);
        validateUniqueEmailAndUsername(username, email);

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(UserRole.USER)
                .userType(userType)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.debug("Obteniendo todos los usuarios activos");
        return userRepository.findAllActive()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.debug("Buscando usuario por ID: {}", id);
        return userMapper.toDto(userRepository.findByIdActive(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByExternalId(String externalId) {
        log.debug("Buscando usuario por externalId: {}", externalId);
        User user = userRepository.findByExternalIdAndDeletedAtIsNull(externalId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(UserRole role) {
        log.debug("Buscando usuarios con rol: {}", role);
        return userRepository.findByRoleAndDeletedAtIsNull(role)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Eliminando usuario con ID: {}", id);
        User user = userRepository.findByIdActive(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        user.softDelete();
        userRepository.save(user);
        log.info("Usuario eliminado exitosamente: {}", id);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        log.info("Actualizando usuario con ID: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        validateUserUpdate(existingUser, userUpdateDTO);

        userMapper.updateUserFromDTO(userUpdateDTO, existingUser);

        if (userUpdateDTO.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("Usuario actualizado exitosamente: {}", id);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public UserDTO restoreUser(Long id) {
        log.info("Restaurando usuario con ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        user.restore();
        User restoredUser = userRepository.save(user);
        log.info("Usuario restaurado exitosamente: {}", id);
        return userMapper.toDto(restoredUser);
    }

    @Transactional
    public void permanentlyDeleteUser(Long id) {
        log.info("Eliminando usuario permanentemente con ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        userRepository.delete(user);
        log.info("Usuario eliminado permanentemente: {}", id);
    }

    /* Métodos privados reutilizables */
    protected void validateUniqueEmailAndUsername(String username, String email) {
        if (userRepository.existsByUsernameAndDeletedAtIsNull(username)) {
            throw new UserAlreadyExistsException("username", USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new UserAlreadyExistsException("email", EMAIL_ALREADY_EXISTS);
        }
    }

    private void validateUserRegistration(UserRegisterDTO userRegisterDTO) {
        if (isNullOrEmpty(userRegisterDTO.getUsername())) {
            throw new InvalidUserDataException("username", "El nombre de usuario es obligatorio");
        }
        if (isNullOrEmpty(userRegisterDTO.getEmail())) {
            throw new InvalidUserDataException("email", "El email es obligatorio");
        }
        if (isNullOrEmpty(userRegisterDTO.getPassword())) {
            throw new InvalidUserDataException("password", "La contraseña es obligatoria");
        }
        if (userRegisterDTO.getPassword().length() < 6) {
            throw new InvalidUserDataException("password", INVALID_PASSWORD);
        }
        validateUniqueEmailAndUsername(userRegisterDTO.getUsername(), userRegisterDTO.getEmail());
    }

    private void validateLoginCredentials(String username, String rawPassword) {
        if (isNullOrEmpty(username)) {
            throw new InvalidUserDataException("username", "El nombre de usuario es obligatorio");
        }
        if (isNullOrEmpty(rawPassword)) {
            throw new InvalidUserDataException("password", "La contraseña es obligatoria");
        }
    }

    private void validateUserUpdate(User existingUser, UserUpdateDTO userUpdateDTO) {
        if (userUpdateDTO.getUsername() != null &&
                !existingUser.getUsername().equals(userUpdateDTO.getUsername()) &&
                userRepository.findByUsername(userUpdateDTO.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("username", USERNAME_ALREADY_EXISTS);
        }
        if (userUpdateDTO.getEmail() != null &&
                !existingUser.getEmail().equals(userUpdateDTO.getEmail()) &&
                userRepository.existsByEmail(userUpdateDTO.getEmail())) {
            throw new UserAlreadyExistsException("email", EMAIL_ALREADY_EXISTS);
        }
        if (userUpdateDTO.getPassword() != null && userUpdateDTO.getPassword().length() < 6) {
            throw new InvalidUserDataException("password", INVALID_PASSWORD);
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
