package com.example.fitnesstracker.dto.request.assignment;


import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespondRequestDTO {

    @Size(max = 500, message = "La respuesta no puede exceder 500 caracteres")
    private String response;
}