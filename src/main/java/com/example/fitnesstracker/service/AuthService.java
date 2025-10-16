package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.MemberDTO;
import com.example.fitnesstracker.dto.response.TrainerDTO;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.exception.ResourceNotFoundException;
import com.example.fitnesstracker.exception.UnauthorizedException;
import com.example.fitnesstracker.exception.UserNotFoundException;
import com.example.fitnesstracker.mapper.MemberMapper;
import com.example.fitnesstracker.mapper.TrainerMapper;
import com.example.fitnesstracker.mapper.UserMapper;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.UserRepository;
import com.example.fitnesstracker.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final MemberMapper memberMapper;
    private final TrainerMapper trainerMapper;

    @Transactional(readOnly = true)
    public AuthResponse login(String username, String password) {
        log.info("Intentando login para usuario: {}", username);

        // Buscar usuario por username
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username).orElseThrow(() -> {
            log.warn("Usuario no encontrado: {}", username);
            return new UserNotFoundException("Usuario no encontrado");
        });

        // Validar que esté habilitado
        if (!user.getEnabled()) {
            log.warn("Intento de login con usuario deshabilitado: {}", username);
            throw new UnauthorizedException("Usuario deshabilitado");
        }

        // Validar password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Password incorrecto para usuario: {}", username);
            throw new UnauthorizedException("Credenciales inválidas");
        }

        // Generar token JWT (usar username porque generateToken acepta String)
        String token = jwtTokenProvider.generateToken(user.getUsername());

        log.info("Login exitoso para usuario: {}", username);

        // Mapear User a UserDTO (usar método correcto: toDto)
        UserDTO userDTO = userMapper.toDto(user);

        // Mapear Member o Trainer si existe
        MemberDTO memberDTO = user.getMember() != null ? memberMapper.toDTO(user.getMember()) : null;
        TrainerDTO trainerDTO = user.getTrainer() != null ? trainerMapper.toDTO(user.getTrainer()) : null;

        return AuthResponse.builder().token(token).type("Bearer").user(userDTO).member(memberDTO).trainer(trainerDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public User validateTokenAndGetUser(String token) {
        log.debug("Validando token");

        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Token inválido");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (!user.getEnabled()) {
            throw new UnauthorizedException("Usuario deshabilitado");
        }

        return user;
    }
}
