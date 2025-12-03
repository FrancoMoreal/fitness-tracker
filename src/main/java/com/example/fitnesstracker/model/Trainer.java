package com.example.fitnesstracker.model;

import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "trainers", indexes = { @Index(name = "idx_trainer_user", columnList = "user_id"),
        @Index(name = "idx_trainer_specialty", columnList = "specialty"),
        @Index(name = "idx_trainer_active", columnList = "is_active") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(exclude = { "user", "assignedMembers" }, callSuper = false)
@ToString(exclude = { "user", "assignedMembers" })
public class Trainer extends BaseEntity {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String specialty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String certifications;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false)
    private Boolean isActive;

    @Builder.Default
    @OneToMany(mappedBy = "assignedTrainer", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    private Set<Member> assignedMembers = new HashSet<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void addMember(Member member) {
        if (member == null)
            return;
        if (assignedMembers == null) {
            assignedMembers = new HashSet<>();
        }
        assignedMembers.add(member);
        member.setAssignedTrainer(this);
    }

    public void removeMember(Member member) {
        if (member == null)
            return;
        if (assignedMembers != null) {
            assignedMembers.remove(member);
        }
        if (member != null) {
            member.setAssignedTrainer(null);
        }
    }

    public int getAssignedMembersCount() {
        return assignedMembers == null ? 0 : assignedMembers.size();
    }

    @PrePersist
    public void prePersist() {
        super.onCreate();
        if (isActive == null) {
            isActive = true;
        }
    }
}
