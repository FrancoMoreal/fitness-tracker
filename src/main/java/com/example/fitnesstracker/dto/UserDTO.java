package com.example.fitnesstracker.dto;

import com.example.fitnesstracker.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private Boolean enabled;
    private UserRole role;

    // NO incluir password por seguridad
}