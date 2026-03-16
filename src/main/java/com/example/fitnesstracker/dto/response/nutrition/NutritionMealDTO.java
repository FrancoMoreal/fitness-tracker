package com.example.fitnesstracker.dto.response.nutrition;

import com.example.fitnesstracker.enums.MealType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NutritionMealDTO {

    private Long id;
    private String externalId;
    private MealType mealType;
    private String name;
    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private String foods;
    private String notes;
    private Integer orderInPlan;
}