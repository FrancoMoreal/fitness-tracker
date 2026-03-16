package com.example.fitnesstracker.dto.request.nutrition;

import com.example.fitnesstracker.enums.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddNutritionMealDTO {

    @NotNull(message = "El tipo de comida es obligatorio")
    private MealType mealType;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double fat;

    private String foods;
    private String notes;

    @NotNull(message = "El orden es obligatorio")
    private Integer orderInPlan;
}