package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.enums.WorkoutPlanStatus;
import com.example.fitnesstracker.model.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

    List<WorkoutPlan> findByMember_IdAndDeletedAtIsNull(Long memberId);

    List<WorkoutPlan> findByTrainer_IdAndDeletedAtIsNull(Long trainerId);

    List<WorkoutPlan> findByMember_IdAndStatusAndDeletedAtIsNull(Long memberId, WorkoutPlanStatus status);

    @Query("SELECT wp FROM WorkoutPlan wp WHERE wp.member.id = :memberId AND wp.status = 'ACTIVE' AND wp.deletedAt IS NULL ORDER BY wp.startDate DESC")
    List<WorkoutPlan> findActivePlansByMember(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(wp) FROM WorkoutPlan wp WHERE wp.trainer.id = :trainerId AND wp.status = 'ACTIVE' AND wp.deletedAt IS NULL")
    long countActivePlansByTrainer(@Param("trainerId") Long trainerId);
}