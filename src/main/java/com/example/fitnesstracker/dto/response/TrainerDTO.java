package com.example.fitnesstracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainerDTO {

    private Long id;
    private String externalId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String specialty;
    private List<String> certifications;
    private BigDecimal hourlyRate;
    private Boolean isActive;
    private Integer assignedMembersCount;
}