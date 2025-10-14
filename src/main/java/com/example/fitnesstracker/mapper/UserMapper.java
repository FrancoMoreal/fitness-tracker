package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserDTO toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enable", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(UserRegisterDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUserFromDTO(UserUpdateDTO dto, @MappingTarget User user);
}
