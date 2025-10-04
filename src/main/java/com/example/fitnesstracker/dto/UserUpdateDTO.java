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
public class UserUpdateDTO {

    private String username;
    private String email;
    private String password; // Opcional, solo si se quiere cambiar
    private Boolean enabled; // Para habilitar/deshabilitar usuarios
    private UserRole role;   // Para cambiar el rol (solo admins deberían poder)
}