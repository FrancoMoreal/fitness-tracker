package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDto(User entity) {
        if (entity == null) {
            return null;
        }

        return UserDTO.builder().id(entity.getId()).externalId(entity.getExternalId()).username(entity.getUsername())
                .email(entity.getEmail()).role(entity.getRole()).enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }

    public User toEntity(UserRegisterDTO dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        // NO setear role ni enabled aquí (lo hace UserService)
        // password se setea en UserService después de encriptar
        return user;
    }

    public void updateUserFromDTO(UserUpdateDTO dto, User user) {
        if (dto == null || user == null) {
            return;
        }
        // Actualizar sólo si el DTO trae valores no nulos
        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        // El DTO usa 'enable' en los tests: comprobar y aplicar si no es null
        if (dto.getEnable() != null) {
            user.setEnabled(dto.getEnable());
        }
        // NO actualizar id ni password aquí; password la maneja UserService
    }
}
