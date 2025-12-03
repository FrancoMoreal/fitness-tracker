package com.example.fitnesstracker.model;

import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_days", indexes = {
        @Index(name = "idx_day_plan", columnList = "workout_plan_id"),
        @Index(name = "idx_day_number", columnList = "day_number")
})

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
//@EqualsAndHashCode(callSuper = false, exclude = {"workoutPlan", "exercises"})
//@ToString(exclude = {"workoutPlan", "exercises"})
public class WorkoutDay extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    private WorkoutPlan workoutPlan;

    @Column(nullable = false, length = 100)
    private String dayName;

    @Column(nullable = false)
    private Integer dayNumber;

    @OneToMany(mappedBy = "workoutDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkoutExercise> exercises = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Helper methods
    public void addExercise(WorkoutExercise exercise) {
        exercises.add(exercise);
        exercise.setWorkoutDay(this);
    }

    public void removeExercise(WorkoutExercise exercise) {
        exercises.remove(exercise);
        exercise.setWorkoutDay(null);
    }
}