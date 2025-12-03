package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.response.workout.WorkoutDayDTO;
import com.example.fitnesstracker.model.WorkoutDay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WorkoutDayMapper {

    private final WorkoutExerciseMapper workoutExerciseMapper;

    public WorkoutDayDTO toDTO(WorkoutDay entity, boolean includeExercises) {
        if (entity == null) {
            return null;
        }

        return WorkoutDayDTO.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .dayName(entity.getDayName())
                .dayNumber(entity.getDayNumber())
                .notes(entity.getNotes())
                .totalExercises(entity.getExercises() != null ? entity.getExercises().size() : 0)
                .exercises(includeExercises && entity.getExercises() != null
                        ? entity.getExercises().stream()
                        .map(workoutExerciseMapper::toDTO)
                        .collect(Collectors.toList())
                        : null)
                .build();
    }

    public WorkoutDayDTO toDTO(WorkoutDay entity) {
        return toDTO(entity, false);
    }
}