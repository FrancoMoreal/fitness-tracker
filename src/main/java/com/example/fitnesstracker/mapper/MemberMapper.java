package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.request.member.RegisterMemberDTO;
import com.example.fitnesstracker.dto.request.member.UpdateMemberDTO;
import com.example.fitnesstracker.dto.response.MemberDTO;
import com.example.fitnesstracker.model.Member;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class MemberMapper {

    public MemberDTO toDTO(Member entity) {
        if (entity == null) {
            return null;
        }

        int remainingDays = 0;
        LocalDate today = LocalDate.now();
        if (entity.getMembershipEndDate() != null && entity.getMembershipEndDate().isAfter(today)) {
            remainingDays = (int) ChronoUnit.DAYS.between(today, entity.getMembershipEndDate());
        }

        return MemberDTO.builder().id(entity.getId()).externalId(entity.getExternalId())
                .firstName(entity.getFirstName()).lastName(entity.getLastName()).fullName(entity.getFullName())
                .phone(entity.getPhone()).dateOfBirth(entity.getDateOfBirth())
                .membershipStartDate(entity.getMembershipStartDate()).membershipEndDate(entity.getMembershipEndDate())
                .remainingDays(remainingDays).height(entity.getHeight()).weight(entity.getWeight())
                .isActive(entity.isActive())
                .assignedTrainerId(entity.getAssignedTrainer() != null ? entity.getAssignedTrainer().getId() : null)
                .build();
    }

    public Member toEntity(RegisterMemberDTO dto) {
        if (dto == null) {
            return null;
        }

        return Member.builder().firstName(dto.getFirstName()).lastName(dto.getLastName()).phone(dto.getPhone())
                .dateOfBirth(dto.getDateOfBirth()).build();
    }

    public void updateFromDTO(UpdateMemberDTO dto, Member entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getFirstName() != null) {
            entity.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            entity.setLastName(dto.getLastName());
        }
        if (dto.getPhone() != null) {
            entity.setPhone(dto.getPhone());
        }
        if (dto.getHeight() != null) {
            entity.setHeight(dto.getHeight());
        }
        if (dto.getWeight() != null) {
            entity.setWeight(dto.getWeight());
        }

    }
}
