package com.example.fitnesstracker.repository;


import com.example.fitnesstracker.model.WorkoutDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutDayRepository extends JpaRepository<WorkoutDay, Long> {

    List<WorkoutDay> findByWorkoutPlan_IdAndDeletedAtIsNullOrderByDayNumberAsc(Long workoutPlanId);
}