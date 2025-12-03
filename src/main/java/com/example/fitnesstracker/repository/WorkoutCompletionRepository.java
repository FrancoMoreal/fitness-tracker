package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.model.WorkoutCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutCompletionRepository extends JpaRepository<WorkoutCompletion, Long> {

    List<WorkoutCompletion> findByMember_IdAndDeletedAtIsNullOrderByCompletedAtDesc(Long memberId);

    @Query("SELECT wc FROM WorkoutCompletion wc WHERE wc.member.id = :memberId AND wc.completedAt BETWEEN :startDate AND :endDate AND wc.deletedAt IS NULL")
    List<WorkoutCompletion> findByMemberAndDateRange(@Param("memberId") Long memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(wc) FROM WorkoutCompletion wc WHERE wc.member.id = :memberId AND wc.deletedAt IS NULL")
    long countCompletionsByMember(@Param("memberId") Long memberId);
}