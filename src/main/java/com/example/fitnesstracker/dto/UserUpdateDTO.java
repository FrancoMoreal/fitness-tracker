package com.example.fitnesstracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    private String username;
    private String email;
    private String password; // si querés actualizar
    private Boolean enable; // activar/desactivar el usuario
}
