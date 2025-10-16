package com.example.fitnesstracker.model;

import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "members", indexes = {
        @Index(name = "idx_member_user", columnList = "user_id"),
        @Index(name = "idx_member_trainer", columnList = "trainer_id"),
        @Index(name = "idx_member_phone", columnList = "phone"),
        @Index(name = "idx_member_status", columnList = "membership_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"user", "assignedTrainer"}, callSuper = false)
@ToString(exclude = {"user", "assignedTrainer"})
public class Member extends BaseEntity {

    @OneToOne(fetch = FetchType.EAGER)
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

    @Column
    private String emergencyContact;

    @Column(nullable = false)
    private LocalDate membershipStartDate;

    @Column(nullable = false)
    private LocalDate membershipEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private Trainer assignedTrainer;

    @Column
    private Double height;

    @Column
    private Double weight;

    @Column(length = 3)
    private String bloodType;

    @Column
    private Boolean isActive;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
