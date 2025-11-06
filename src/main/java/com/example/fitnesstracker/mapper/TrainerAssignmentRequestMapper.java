package com.example.fitnesstracker.mapper;


import com.example.fitnesstracker.dto.response.assignment.TrainerAssignmentRequestDTO;
import com.example.fitnesstracker.model.TrainerAssignmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainerAssignmentRequestMapper {

    public TrainerAssignmentRequestDTO toDTO(TrainerAssignmentRequest entity) {
        if (entity == null) {
            return null;
        }

        return TrainerAssignmentRequestDTO.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .memberId(entity.getMember().getId())
                .memberName(entity.getMember().getFullName())
                .trainerId(entity.getTrainer().getId())
                .trainerName(entity.getTrainer().getFullName())
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .respondedAt(entity.getRespondedAt())
                .memberMessage(entity.getMemberMessage())
                .trainerResponse(entity.getTrainerResponse())
                .build();
    }
}
