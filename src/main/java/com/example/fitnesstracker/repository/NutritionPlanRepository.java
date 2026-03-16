package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.enums.NutritionPlanStatus;
import com.example.fitnesstracker.model.NutritionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, Long> {

    List<NutritionPlan> findByMember_IdAndDeletedAtIsNull(Long memberId);

    List<NutritionPlan> findByTrainer_IdAndDeletedAtIsNull(Long trainerId);

    List<NutritionPlan> findByMember_IdAndStatusAndDeletedAtIsNull(Long memberId, NutritionPlanStatus status);

    @Query("SELECT np FROM NutritionPlan np WHERE np.member.id = :memberId AND np.status = 'ACTIVE' AND np.deletedAt IS NULL ORDER BY np.startDate DESC")
    List<NutritionPlan> findActivePlansByMember(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(np) FROM NutritionPlan np WHERE np.trainer.id = :trainerId AND np.status = 'ACTIVE' AND np.deletedAt IS NULL")
    long countActivePlansByTrainer(@Param("trainerId") Long trainerId);
}