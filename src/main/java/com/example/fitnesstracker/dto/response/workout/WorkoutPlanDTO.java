package com.example.fitnesstracker.dto.response.workout;

import com.example.fitnesstracker.enums.WorkoutPlanStatus;
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
public class WorkoutPlanDTO {

    private Long id;
    private String externalId;
    private String name;
    private String description;
    private Long memberId;
    private String memberName;
    private Long trainerId;
    private String trainerName;
    private WorkoutPlanStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private Integer totalDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<WorkoutDayDTO> workoutDays;
}