package com.example.fitnesstracker.model;


import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exercise_logs", indexes = {
        @Index(name = "idx_log_completion", columnList = "workout_completion_id"),
        @Index(name = "idx_log_exercise", columnList = "workout_exercise_id")
})

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
//@EqualsAndHashCode(callSuper = false, exclude = {"workoutCompletion", "workoutExercise"})
//@ToString(exclude = {"workoutCompletion", "workoutExercise"})
public class ExerciseLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_completion_id", nullable = false)
    private WorkoutCompletion workoutCompletion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    @Column(nullable = false)
    private Integer setsCompleted;

    @Column(nullable = false)
    private Integer repsCompleted;

    private Double weightUsed;

    @Column(columnDefinition = "TEXT")
    private String notes;
}