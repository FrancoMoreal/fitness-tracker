package com.example.fitnesstracker.model;

import jakarta.persistence.*;
import lombok.*;
@Data
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // opcional, para crear objetos con User.builder()...
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Strign role = "USER";

    // habilitado o no.
    @Column(nullable = false)
    private boolean enable = true;

    // fecha de creacion
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

}
