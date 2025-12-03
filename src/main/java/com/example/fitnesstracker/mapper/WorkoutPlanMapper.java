package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.response.workout.WorkoutPlanDTO;
import com.example.fitnesstracker.model.WorkoutPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WorkoutPlanMapper {

    private final WorkoutDayMapper workoutDayMapper;

    public WorkoutPlanDTO toDTO(WorkoutPlan entity, boolean includeWorkoutDays) {
        if (entity == null) {
            return null;
        }

        return WorkoutPlanDTO.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .name(entity.getName())
                .description(entity.getDescription())
                .memberId(entity.getMember().getId())
                .memberName(entity.getMember().getFullName())
                .trainerId(entity.getTrainer().getId())
                .trainerName(entity.getTrainer().getFullName())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .notes(entity.getNotes())
                .totalDays(entity.getWorkoutDays() != null ? entity.getWorkoutDays().size() : 0)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .workoutDays(includeWorkoutDays && entity.getWorkoutDays() != null
                        ? entity.getWorkoutDays().stream()
                        .map(day -> workoutDayMapper.toDTO(day, true))
                        .collect(Collectors.toList())
                        : null)
                .build();
    }

    public WorkoutPlanDTO toDTO(WorkoutPlan entity) {
        return toDTO(entity, false);
    }
}