package com.example.fitnesstracker.model;

import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_completions", indexes = {
        @Index(name = "idx_completion_member", columnList = "member_id"),
        @Index(name = "idx_completion_day", columnList = "workout_day_id"),
        @Index(name = "idx_completion_date", columnList = "completed_at")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
// @EqualsAndHashCode(callSuper = false, exclude = {"member", "workoutDay", "exerciseLogs"})
// @ToString(exclude = {"member", "workoutDay", "exerciseLogs"})
public class WorkoutCompletion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_day_id", nullable = false)
    private WorkoutDay workoutDay;

    @Column(nullable = false)
    private LocalDate completedAt;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "workoutCompletion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExerciseLog> exerciseLogs = new ArrayList<>();

    // Helper methods
    public void addExerciseLog(ExerciseLog log) {
        exerciseLogs.add(log);
        log.setWorkoutCompletion(this);
    }
}