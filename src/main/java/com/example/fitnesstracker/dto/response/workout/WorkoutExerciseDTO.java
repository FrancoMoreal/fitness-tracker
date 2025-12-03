package com.example.fitnesstracker.dto.response.workout;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkoutExerciseDTO {

    private Long id;
    private String externalId;
    private Long exerciseId;
    private String exerciseName;
    private Integer sets;
    private Integer reps;
    private Double weight;
    private Integer restSeconds;
    private Integer orderInWorkout;
    private String notes;
}