package com.example.fitnesstracker.model;

import com.example.fitnesstracker.enums.AssignmentStatus;
import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "members", indexes = {
        @Index(name = "idx_member_user", columnList = "user_id"),
        @Index(name = "idx_member_trainer", columnList = "trainer_id"),
        @Index(name = "idx_member_phone", columnList = "phone"),
        @Index(name = "idx_member_membership_end", columnList = "membership_end_date")
})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(exclude = {"user", "assignedTrainer"}, callSuper = false)
@ToString(exclude = {"user", "assignedTrainer"})
public class Member extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private LocalDate membershipStartDate;

    @Column(nullable = false)
    private LocalDate membershipEndDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus assignmentStatus = AssignmentStatus.NO_TRAINER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private Trainer assignedTrainer;

    @Column
    private Double height;

    @Column
    private Double weight;


    // ========== Helper Methods ==========
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Calcula la edad actual
     */
    public Integer getAge() {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Calcula el IMC (Índice de Masa Corporal)
     */
    public Double calculateBMI() {
        if (weight == null || height == null || height == 0) {
            return null;
        }
        return weight / (height * height);
    }

    /**
     * Verifica si la membresía está vigente
     */
    public boolean isMembershipActive() {
        if (membershipEndDate == null) return false;
        return !LocalDate.now().isAfter(membershipEndDate) && this.isActive();
    }

    /**
     * Verifica si tiene trainer asignado
     */
    public boolean hasTrainer() {
        return assignedTrainer != null && assignedTrainer.isActive();
    }
}
