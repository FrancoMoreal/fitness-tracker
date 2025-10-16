package com.example.fitnesstracker.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberDTO {

    private Long id;
    private String externalId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
    private LocalDate membershipStartDate;
    private LocalDate membershipEndDate;
    private Integer remainingDays;
    private Double height;
    private Double weight;
    private Boolean isActive;
    private Long assignedTrainerId;
}