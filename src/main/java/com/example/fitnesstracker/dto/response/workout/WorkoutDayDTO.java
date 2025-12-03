package com.example.fitnesstracker.dto.response.workout;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkoutDayDTO {

    private Long id;
    private String externalId;
    private String dayName;
    private Integer dayNumber;
    private String notes;
    private Integer totalExercises;
    private List<WorkoutExerciseDTO> exercises;
}