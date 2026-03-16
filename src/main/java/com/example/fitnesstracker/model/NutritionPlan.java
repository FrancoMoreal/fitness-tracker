package com.example.fitnesstracker.model;

import com.example.fitnesstracker.enums.NutritionPlanStatus;
import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nutrition_plans", indexes = {
        @Index(name = "idx_nutrition_member", columnList = "member_id"),
        @Index(name = "idx_nutrition_trainer", columnList = "trainer_id"),
        @Index(name = "idx_nutrition_status", columnList = "status")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class NutritionPlan extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NutritionPlanStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "nutritionPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NutritionMeal> meals = new ArrayList<>();

    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.status == null) {
            this.status = NutritionPlanStatus.DRAFT;
        }
    }

    public void addMeal(NutritionMeal meal) {
        meals.add(meal);
        meal.setNutritionPlan(this);
    }

    public void removeMeal(NutritionMeal meal) {
        meals.remove(meal);
        meal.setNutritionPlan(null);
    }
}