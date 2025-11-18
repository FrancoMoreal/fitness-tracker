package com.example.fitnesstracker.dto.response.exercise;

import com.example.fitnesstracker.enums.DifficultyLevel;
import com.example.fitnesstracker.enums.ExerciseCategory;
import com.example.fitnesstracker.enums.MuscleGroup;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExerciseDTO {

    private Long id;
    private String externalId;
    private String name;
    private String description;
    private ExerciseCategory category;
    private MuscleGroup primaryMuscle;
    private String videoUrl;
    private String imageUrl;
    private DifficultyLevel difficulty;
    private Boolean isCustom;
    private Long createdByTrainerId;
    private String createdByTrainerName;
    private String instructions;
    private String equipment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}