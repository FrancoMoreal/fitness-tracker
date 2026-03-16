package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.model.NutritionMeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionMealRepository extends JpaRepository<NutritionMeal, Long> {

    List<NutritionMeal> findByNutritionPlan_IdAndDeletedAtIsNullOrderByOrderInPlanAsc(Long planId);
}