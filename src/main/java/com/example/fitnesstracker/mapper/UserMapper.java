package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.*;
import com.example.fitnesstracker.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    // Registro
    User toEntity(UserRegisterDTO dto);
    UserRegisterDTO toRegisterDto(User user);

    // Consulta
    UserDTO toDto(User user);

    // Actualización (sin password)
    void updateUserFromDTO(UserUpdateDTO dto, @MappingTarget User user);

    // Soporte para listas
    List<UserDTO> toDtoList(List<User> users);
    List<UserRegisterDTO> toRegisterDtoList(List<User> users);

}
