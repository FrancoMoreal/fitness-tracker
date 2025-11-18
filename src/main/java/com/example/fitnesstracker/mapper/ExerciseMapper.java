package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.request.exercise.CreateExerciseDTO;
import com.example.fitnesstracker.dto.request.exercise.UpdateExerciseDTO;
import com.example.fitnesstracker.dto.response.exercise.ExerciseDTO;
import com.example.fitnesstracker.model.Exercise;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExerciseMapper {

    public ExerciseDTO toDTO(Exercise entity) {
        if (entity == null) {
            return null;
        }

        return ExerciseDTO.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .primaryMuscle(entity.getPrimaryMuscle())
                .videoUrl(entity.getVideoUrl())
                .imageUrl(entity.getImageUrl())
                .difficulty(entity.getDifficulty())
                .isCustom(entity.getIsCustom())
                .createdByTrainerId(entity.getCreatedByTrainer() != null ? entity.getCreatedByTrainer().getId() : null)
                .createdByTrainerName(entity.getCreatedByTrainer() != null ? entity.getCreatedByTrainer().getFullName() : null)
                .instructions(entity.getInstructions())
                .equipment(entity.getEquipment())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public Exercise toEntity(CreateExerciseDTO dto) {
        if (dto == null) {
            return null;
        }

        return Exercise.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .primaryMuscle(dto.getPrimaryMuscle())
                .videoUrl(dto.getVideoUrl())
                .imageUrl(dto.getImageUrl())
                .difficulty(dto.getDifficulty())
                .instructions(dto.getInstructions())
                .equipment(dto.getEquipment())
                .build();
    }

    public void updateFromDTO(UpdateExerciseDTO dto, Exercise entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setPrimaryMuscle(dto.getPrimaryMuscle());
        entity.setVideoUrl(dto.getVideoUrl());
        entity.setImageUrl(dto.getImageUrl());
        entity.setDifficulty(dto.getDifficulty());
        entity.setInstructions(dto.getInstructions());
        entity.setEquipment(dto.getEquipment());
    }
}

