package com.example.fitnesstracker.model;

import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workout_exercises", indexes = {
        @Index(name = "idx_workout_ex_day", columnList = "workout_day_id"),
        @Index(name = "idx_workout_ex_exercise", columnList = "exercise_id"),
        @Index(name = "idx_workout_ex_order", columnList = "order_in_workout")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
//@EqualsAndHashCode(callSuper = false, exclude = {"workoutDay", "exercise"})
//@ToString(exclude = {"workoutDay", "exercise"})
public class WorkoutExercise extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_day_id", nullable = false)
    private WorkoutDay workoutDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private Integer sets;

    @Column(nullable = false)
    private Integer reps;
    @Column
    private Double weight;
    @Column
    private Integer restSeconds;

    @Column(nullable = false)
    private Integer orderInWorkout;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
