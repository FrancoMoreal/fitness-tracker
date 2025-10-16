package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Búsquedas básicas
    Optional<Member> findByUser_Id(Long userId);
    Optional<Member> findByExternalId(String externalId);
    Optional<Member> findByPhone(String phone);
    List<Member> findByIsActiveAndDeletedAtIsNull(Boolean isActive);

    // Búsquedas por trainer
    List<Member> findByAssignedTrainer_Id(Long trainerId);
    List<Member> findByAssignedTrainer_IdAndDeletedAtIsNull(Long trainerId);

    // Búsquedas avanzadas (sin usar enums, se usa isActive)
    @Query("SELECT m FROM Member m WHERE m.membershipEndDate < :date AND m.isActive = true AND m.deletedAt IS NULL")
    List<Member> findExpiredMemberships(@Param("date") LocalDate date);

    @Query("SELECT m FROM Member m WHERE m.membershipEndDate BETWEEN :startDate AND :endDate AND m.deletedAt IS NULL")
    List<Member> findMembershipsExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM Member m WHERE m.isActive = true AND m.deletedAt IS NULL")
    List<Member> findAllActiveMembersWithActiveMembership();

    @Query("SELECT COUNT(m) FROM Member m WHERE m.assignedTrainer.id = :trainerId AND m.deletedAt IS NULL")
    long countMembersByTrainer(@Param("trainerId") Long trainerId);

    // Búsqueda por nombre
    @Query("SELECT m FROM Member m WHERE (LOWER(m.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(m.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) AND m.deletedAt IS NULL")
    List<Member> searchByName(@Param("search") String search);

    // Existencias
    boolean existsByPhoneAndDeletedAtIsNull(String phone);
    boolean existsByUser_IdAndDeletedAtIsNull(Long userId);

    // Contar miembros (usar isActive en lugar de membershipStatus)
    @Query("SELECT COUNT(m) FROM Member m WHERE m.isActive = true AND m.deletedAt IS NULL")
    long countActiveMembersTotal();

    @Query("SELECT COUNT(m) FROM Member m WHERE m.isActive = true AND m.deletedAt IS NULL")
    long countMembershipsActive();

    @Query("SELECT COUNT(m) FROM Member m WHERE m.deletedAt IS NULL")
    long countAllMembers();
}