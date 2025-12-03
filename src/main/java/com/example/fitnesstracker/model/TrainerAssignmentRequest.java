package com.example.fitnesstracker.model;

import com.example.fitnesstracker.enums.RequestStatus;
import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "trainer_assignment_requests", indexes = {
        @Index(name = "idx_assignment_member", columnList = "member_id"),
        @Index(name = "idx_assignment_trainer", columnList = "trainer_id"),
        @Index(name = "idx_assignment_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"member", "trainer"})
public class TrainerAssignmentRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime respondedAt;

    @Column(length = 500)
    private String memberMessage;  // Mensaje del member al solicitar

    @Column(length = 500)
    private String trainerResponse;  // Respuesta del trainer

    @PrePersist
    public void prePersist() {
        super.onCreate();
        if (this.status == null) {
            this.status = RequestStatus.PENDING;
        }
        if (this.requestedAt == null) {
            this.requestedAt = LocalDateTime.now();
        }
    }
}