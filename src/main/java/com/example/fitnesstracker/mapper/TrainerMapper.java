package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.request.trainer.RegisterTrainerDTO;
import com.example.fitnesstracker.dto.request.trainer.UpdateTrainerDTO;
import com.example.fitnesstracker.dto.response.TrainerDTO;
import com.example.fitnesstracker.model.Trainer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrainerMapper {

    public TrainerDTO toDTO(Trainer entity) {
        if (entity == null) {
            return null;
        }

        // Convertir certifications String a List
        List<String> certificationsList = entity.getCertifications() != null && !entity.getCertifications().isEmpty()
                ? Arrays.asList(entity.getCertifications().split(","))
                : List.of();

        return TrainerDTO.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .fullName(entity.getFullName())
                .specialty(entity.getSpecialty())
                .certifications(certificationsList)
                .hourlyRate(entity.getHourlyRate())
                .isActive(entity.getIsActive())
                .assignedMembersCount(entity.getAssignedMembersCount())
                .build();
    }

    public Trainer toEntity(RegisterTrainerDTO dto) {
        if (dto == null) {
            return null;
        }

        // Convertir List<String> a String
        String certificationsStr = dto.getCertifications() != null && !dto.getCertifications().isEmpty()
                ? String.join(",", dto.getCertifications())
                : "";

        return Trainer.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .specialty(dto.getSpecialty())
                .certifications(certificationsStr)
                .hourlyRate(dto.getHourlyRate())
                .build();
    }

    public void updateFromDTO(UpdateTrainerDTO dto, Trainer entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setSpecialty(dto.getSpecialty());

        if (dto.getHourlyRate() != null) {
            entity.setHourlyRate(dto.getHourlyRate());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }
}