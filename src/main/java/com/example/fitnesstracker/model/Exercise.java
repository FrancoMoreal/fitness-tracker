package com.example.fitnesstracker.model;

import com.example.fitnesstracker.enums.DifficultyLevel;
import com.example.fitnesstracker.enums.ExerciseCategory;
import com.example.fitnesstracker.enums.MuscleGroup;
import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exercises", indexes = {
        @Index(name = "idx_exercise_category", columnList = "category"),
        @Index(name = "idx_exercise_muscle", columnList = "primary_muscle"),
        @Index(name = "idx_exercise_difficulty", columnList = "difficulty"),
        @Index(name = "idx_exercise_custom", columnList = "is_custom"),
        @Index(name = "idx_exercise_creator", columnList = "created_by_trainer_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "createdByTrainer")
public class Exercise extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExerciseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_muscle", nullable = false)
    private MuscleGroup primaryMuscle;

    @Column(length = 500)
    private String videoUrl;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficulty;

    @Column(nullable = false)
    private Boolean isCustom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_trainer_id")
    private Trainer createdByTrainer;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(columnDefinition = "TEXT")
    private String equipment;

    @PrePersist
    public void prePersist() {
        super.onCreate();
        if (this.isCustom == null) {
            this.isCustom = false;
        }
    }
}