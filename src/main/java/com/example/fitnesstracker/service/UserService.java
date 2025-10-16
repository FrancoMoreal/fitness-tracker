package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.security.JwtTokenProvider;
import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.UserAlreadyExistsException;
import com.example.fitnesstracker.exception.UserNotFoundException;
import com.example.fitnesstracker.mapper.UserMapper;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserDTO registerUser(UserRegisterDTO userRegisterDTO) {
        validateUserRegistration(userRegisterDTO);

        User user = userMapper.toEntity(userRegisterDTO);
        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        user.setRole(UserRole.USER);
        user.setEnabled(true);

        return userMapper.toDto(userRepository.save(user));
    }

    public AuthResponse login(String username, String rawPassword) {
        validateLoginCredentials(username, rawPassword);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, rawPassword)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("No se encontró usuario con username: " + username));

        return new AuthResponse(token, userMapper.toDto(user));
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllActive()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return userMapper.toDto(userRepository.findByIdActive(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByExternalId(String externalId) {
        return userMapper.toDto(userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new UserNotFoundException("No se encontró usuario con external ID: " + externalId)));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findByIdActive(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.softDelete();
        userRepository.save(user);
    }

    @Transactional
    public UserDTO restoreUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (user.isActive()) {
            throw new InvalidUserDataException("El usuario no está eliminado");
        }

        user.restore();
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public void permanentlyDeleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        validateUserUpdate(existingUser, userUpdateDTO);

        userMapper.updateUserFromDTO(userUpdateDTO, existingUser);

        if (userUpdateDTO.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }

        return userMapper.toDto(userRepository.save(existingUser));
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role)
                .stream()
                .map(userMapper::toDto)
                .toList();
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
            throw new InvalidUserDataException("password", "La contraseña debe tener al menos 6 caracteres");
        }
        if (userRepository.findByUsername(userRegisterDTO.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("username", userRegisterDTO.getUsername());
        }
        if (userRepository.existsByEmail(userRegisterDTO.getEmail())) {
            throw new UserAlreadyExistsException("email", userRegisterDTO.getEmail());
        }
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
            throw new UserAlreadyExistsException("username", userUpdateDTO.getUsername());
        }
        if (userUpdateDTO.getEmail() != null &&
                !existingUser.getEmail().equals(userUpdateDTO.getEmail()) &&
                userRepository.existsByEmail(userUpdateDTO.getEmail())) {
            throw new UserAlreadyExistsException("email", userUpdateDTO.getEmail());
        }
        if (userUpdateDTO.getPassword() != null && userUpdateDTO.getPassword().length() < 6) {
            throw new InvalidUserDataException("password", "La contraseña debe tener al menos 6 caracteres");
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}