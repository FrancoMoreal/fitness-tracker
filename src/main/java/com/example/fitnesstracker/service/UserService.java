package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.UserDTO;
import com.example.fitnesstracker.dto.UserRegisterDTO;
import com.example.fitnesstracker.dto.UserUpdateDTO;
import com.example.fitnesstracker.mapper.UserMapper;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    // Registro de usuario -> retorna DTO
    public UserDTO registerUser(UserRegisterDTO userRegisterDTO) {
        User user = userMapper.toEntity(userRegisterDTO);
        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    // Login
    public boolean login(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }

    // Obtener todos los usuarios
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    // Obtener usuario por id
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    // Eliminar usuario
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Actualizar usuario
    public Optional<UserDTO> updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        return userRepository.findById(id)
                .map(user -> {
                    // Actualizamos los campos normales
                    userMapper.updateUserFromDTO(userUpdateDTO, user);

                    // Encriptar password si viene en el DTO
                    if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isBlank()) {
                        user.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
                    }

                    User updated = userRepository.save(user);
                    return userMapper.toDto(updated);
                });
    }



}
