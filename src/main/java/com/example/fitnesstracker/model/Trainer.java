package com.example.fitnesstracker.model;

import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
/**
 * Entidad que representa a un entrenador en el sistema.
 * Un entrenador está asociado a un usuario (User) que contiene
 * la información de autenticación y puede tener múltiples miembros (Member)
 * asignados a él.
 */
@Entity
@Table(name = "trainers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Trainer extends BaseEntity {


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(nullable = false, length = 100)
    private String specialty;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Member> members = new ArrayList<>();

    public void addMember(Member member) {
        if (member != null) {
            this.members.add(member);
            member.setTrainer(this);
        }
    }

    public void removeMember(Member member) {
        if (member != null) {
            this.members.remove(member);
            member.setTrainer(null);
        }
    }
}