package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.model.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    List<WorkoutExercise> findByWorkoutDay_IdAndDeletedAtIsNullOrderByOrderInWorkoutAsc(Long workoutDayId);
}