package com.example.fitnesstracker.dto.response.workout;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkoutCompletionDTO {

    private Long id;
    private String externalId;
    private Long memberId;
    private String memberName;
    private Long workoutDayId;
    private String workoutDayName;
    private LocalDate completedAt;
    private Integer rating;
    private String notes;
    private LocalDateTime createdAt;
    private List<ExerciseLogDetailDTO> exerciseLogs;
}