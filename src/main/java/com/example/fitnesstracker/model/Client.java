package com.example.fitnesstracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Client {
    @Id
    private Long id;

    @OneToOne
    private Coach coach;
}
