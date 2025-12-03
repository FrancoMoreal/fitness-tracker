package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.response.workout.WorkoutExerciseDTO;
import com.example.fitnesstracker.model.WorkoutExercise;
import org.springframework.stereotype.Component;

@Component
public class WorkoutExerciseMapper {

    public WorkoutExerciseDTO toDTO(WorkoutExercise entity) {
        if (entity == null) {
            return null;
        }

        return WorkoutExerciseDTO.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .exerciseId(entity.getExercise().getId())
                .exerciseName(entity.getExercise().getName())
                .sets(entity.getSets())
                .reps(entity.getReps())
                .weight(entity.getWeight())
                .restSeconds(entity.getRestSeconds())
                .orderInWorkout(entity.getOrderInWorkout())
                .notes(entity.getNotes())
                .build();
    }
}