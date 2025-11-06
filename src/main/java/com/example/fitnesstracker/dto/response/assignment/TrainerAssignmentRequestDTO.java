package com.example.fitnesstracker.dto.response.assignment;

import com.example.fitnesstracker.enums.RequestStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainerAssignmentRequestDTO {

    private Long id;
    private String externalId;
    private Long memberId;
    private String memberName;
    private Long trainerId;
    private String trainerName;
    private RequestStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private String memberMessage;
    private String trainerResponse;
}