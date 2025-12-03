package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.response.workout.ExerciseLogDetailDTO;
import com.example.fitnesstracker.dto.response.workout.WorkoutCompletionDTO;
import com.example.fitnesstracker.model.WorkoutCompletion;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class WorkoutCompletionMapper {

    public WorkoutCompletionDTO toDTO(WorkoutCompletion entity) {
        if (entity == null) {
            return null;
        }

        return WorkoutCompletionDTO.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .memberId(entity.getMember().getId())
                .memberName(entity.getMember().getFullName())
                .workoutDayId(entity.getWorkoutDay().getId())
                .workoutDayName(entity.getWorkoutDay().getDayName())
                .completedAt(entity.getCompletedAt())
                .rating(entity.getRating())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .exerciseLogs(entity.getExerciseLogs() != null
                        ? entity.getExerciseLogs().stream()
                        .map(log -> ExerciseLogDetailDTO.builder()
                                .id(log.getId())
                                .exerciseName(log.getWorkoutExercise().getExercise().getName())
                                .setsCompleted(log.getSetsCompleted())
                                .repsCompleted(log.getRepsCompleted())
                                .weightUsed(log.getWeightUsed())
                                .notes(log.getNotes())
                                .build())
                        .collect(Collectors.toList())
                        : null)
                .build();
    }
}