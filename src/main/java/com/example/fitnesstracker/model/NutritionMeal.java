package com.example.fitnesstracker.model;

import com.example.fitnesstracker.enums.MealType;
import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nutrition_meals", indexes = {
        @Index(name = "idx_meal_plan", columnList = "nutrition_plan_id"),
        @Index(name = "idx_meal_type", columnList = "meal_type")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class NutritionMeal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_plan_id", nullable = false)
    private NutritionPlan nutritionPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Column(nullable = false, length = 100)
    private String name;

    // Macros opcionales
    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double fat;

    // Alimentos en texto libre (ej: "200g pollo, 1 taza arroz integral")
    @Column(columnDefinition = "TEXT")
    private String foods;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Integer orderInPlan;
}