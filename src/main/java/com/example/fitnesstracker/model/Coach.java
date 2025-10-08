package com.example.fitnesstracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity
public class Coach {
    @Id
    private Long id;

    @OneToMany

    private List<Client> clients;
}
