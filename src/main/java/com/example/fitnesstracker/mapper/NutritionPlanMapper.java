package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.response.nutrition.NutritionMealDTO;
import com.example.fitnesstracker.dto.response.nutrition.NutritionPlanDTO;
import com.example.fitnesstracker.model.NutritionMeal;
import com.example.fitnesstracker.model.NutritionPlan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NutritionPlanMapper {

    public NutritionPlanDTO toDTO(NutritionPlan plan, boolean withMeals) {
        NutritionPlanDTO.NutritionPlanDTOBuilder builder = NutritionPlanDTO.builder()
                .id(plan.getId())
                .externalId(plan.getExternalId() != null ? plan.getExternalId().toString() : null)
                .name(plan.getName())
                .description(plan.getDescription())
                .memberId(plan.getMember().getId())
                .memberName(plan.getMember().getFirstName() + " " + plan.getMember().getLastName())
                .trainerId(plan.getTrainer().getId())
                .trainerName(plan.getTrainer().getFirstName() + " " + plan.getTrainer().getLastName())
                .status(plan.getStatus())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .notes(plan.getNotes())
                .totalMeals(plan.getMeals() != null ? plan.getMeals().size() : 0)
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt());

        if (withMeals && plan.getMeals() != null) {
            List<NutritionMealDTO> mealDTOs = plan.getMeals().stream()
                    .filter(m -> m.getDeletedAt() == null)
                    .map(this::toMealDTO)
                    .collect(Collectors.toList());
            builder.meals(mealDTOs);
        }

        return builder.build();
    }

    public NutritionMealDTO toMealDTO(NutritionMeal meal) {
        return NutritionMealDTO.builder()
                .id(meal.getId())
                .externalId(meal.getExternalId() != null ? meal.getExternalId().toString() : null)
                .mealType(meal.getMealType())
                .name(meal.getName())
                .calories(meal.getCalories())
                .protein(meal.getProtein())
                .carbs(meal.getCarbs())
                .fat(meal.getFat())
                .foods(meal.getFoods())
                .notes(meal.getNotes())
                .orderInPlan(meal.getOrderInPlan())
                .build();
    }
}