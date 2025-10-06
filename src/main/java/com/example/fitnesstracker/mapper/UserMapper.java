package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface UserMapper {

    /**
     * Convierte User a UserDTO
     * Excluye el password por seguridad
     */
   // @Mapping(target = "password", ignore = true)
    UserDTO toDto(User user);

    /**
     * Convierte UserRegisterDTO a User
     * El password se encripta en el Service
     * El role se asigna por defecto en el Service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enable", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(UserRegisterDTO dto);

    /**
     * Actualiza un User existente con datos de UserUpdateDTO
     * Ignora campos null para no sobrescribir valores existentes
     * El password se maneja por separado en el Service
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUserFromDTO(UserUpdateDTO dto, @MappingTarget User user);
}