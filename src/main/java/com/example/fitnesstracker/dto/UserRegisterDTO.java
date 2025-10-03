package com.example.fitnesstracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class UserRegisterDTO {
    private String username;
    private String email;
    private String password;


}

// Cuando alguien se registre pedimos mas datos y no usamos User.