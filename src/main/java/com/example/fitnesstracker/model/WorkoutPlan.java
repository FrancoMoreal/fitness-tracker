package com.example.fitnesstracker.model;

import com.example.fitnesstracker.enums.WorkoutPlanStatus;
import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_plans", indexes = {
        @Index(name = "idx_workout_member", columnList = "member_id"),
        @Index(name = "idx_workout_trainer", columnList = "trainer_id"),
        @Index(name = "idx_workout_status", columnList = "status")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
//@EqualsAndHashCode(callSuper = false, exclude = {"member", "trainer", "workoutDays"})
//@ToString(exclude = {"member", "trainer", "workoutDays"})
public class WorkoutPlan extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkoutPlanStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @OneToMany(mappedBy = "workoutPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkoutDay> workoutDays = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.status == null) {
            this.status = WorkoutPlanStatus.DRAFT;
        }
    }

    // Helper methods
    public void addWorkoutDay(WorkoutDay day) {
        workoutDays.add(day);
        day.setWorkoutPlan(this);
    }

    public void removeWorkoutDay(WorkoutDay day) {
        workoutDays.remove(day);
        day.setWorkoutPlan(null);
    }
}