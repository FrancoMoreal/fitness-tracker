package com.example.fitnesstracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column()
    private String username;
    @Column()
    private String password;
    @Column()
    private String email;
    @Column()
    private Boolean enable = true; // Por defecto true al crear un usuario
    @Column()
    private String role = "USER"; // crear Enum de roles y hacer este campo que sea un Enum.
}