package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.model.ExerciseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {

    List<ExerciseLog> findByWorkoutCompletion_IdAndDeletedAtIsNull(Long completionId);
}