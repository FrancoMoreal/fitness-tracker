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
public class ExerciseLogDetailDTO {

    private Long id;
    private String exerciseName;
    private Integer setsCompleted;
    private Integer repsCompleted;
    private Double weightUsed;
    private String notes;
}
