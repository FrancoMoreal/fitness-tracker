package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUser_Id(Long userId);

    Optional<Member> findByExternalId(String externalId);

    Optional<Member> findByPhone(String phone);

    // ⭐ BÚSQUEDAS OPTIMIZADAS CON JOIN FETCH (NUEVAS)

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT m FROM Member m WHERE m.user.id = :userId AND m.deletedAt IS NULL")
    Optional<Member> findByUserIdWithUser(@Param("userId") Long userId);

    @Query("""
                SELECT m FROM Member m
                LEFT JOIN FETCH m.user u
                LEFT JOIN FETCH m.assignedTrainer t
                LEFT JOIN FETCH t.user tu
                WHERE m.id = :id
                AND m.deletedAt IS NULL
            """)
    Optional<Member> findByIdWithFullProfile(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
                SELECT m FROM Member m
                WHERE m.deletedAt IS NULL
                AND m.membershipEndDate >= :today
                ORDER BY m.firstName, m.lastName
            """)
    List<Member> findAllActiveWithValidMembership(@Param("today") LocalDate today);

    List<Member> findByAssignedTrainer_Id(Long trainerId);

    List<Member> findByAssignedTrainer_IdAndDeletedAtIsNull(Long trainerId);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
                SELECT m FROM Member m
                WHERE m.assignedTrainer.id = :trainerId
                AND m.deletedAt IS NULL
                ORDER BY m.firstName, m.lastName
            """)
    List<Member> findActiveByTrainerIdWithUser(@Param("trainerId") Long trainerId);

    @Query("""
                SELECT m FROM Member m 
                WHERE m.membershipEndDate < :date 
                AND m.deletedAt IS NULL
            """)
    List<Member> findExpiredMemberships(@Param("date") LocalDate date);

    @Query("""
                SELECT m FROM Member m 
                WHERE m.membershipEndDate BETWEEN :startDate AND :endDate 
                AND m.deletedAt IS NULL
                ORDER BY m.membershipEndDate ASC
            """)
    List<Member> findMembersWithExpiringMembership(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
                SELECT m FROM Member m
                WHERE m.assignedTrainer IS NULL
                AND m.deletedAt IS NULL
                AND m.membershipEndDate >= :today
                ORDER BY m.membershipStartDate DESC
            """)
    List<Member> findUnassignedActiveMembers(@Param("today") LocalDate today);

    @Query("SELECT m FROM Member m WHERE m.deletedAt IS NULL")
    List<Member> findAllActiveMembersWithActiveMembership();

    @Query("SELECT COUNT(m) FROM Member m WHERE m.assignedTrainer.id = :trainerId AND m.deletedAt IS NULL")
    long countMembersByTrainer(@Param("trainerId") Long trainerId);

    @Query("""
                SELECT COUNT(m) FROM Member m 
                WHERE m.assignedTrainer.id = :trainerId 
                AND m.deletedAt IS NULL
                AND m.membershipEndDate >= :today
            """)
    long countActiveMembersByTrainerId(
            @Param("trainerId") Long trainerId,
            @Param("today") LocalDate today
    );

    @Query("SELECT COUNT(m) FROM Member m WHERE m.deletedAt IS NULL")
    long countActiveMembersTotal();

    @Query("""
                SELECT COUNT(m) FROM Member m 
                WHERE m.deletedAt IS NULL 
                AND m.membershipEndDate >= :today
            """)
    long countMembershipsActive(@Param("today") LocalDate today);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.deletedAt IS NULL")
    long countAllMembers();

    @Query("""
                SELECT m FROM Member m 
                WHERE (LOWER(m.firstName) LIKE LOWER(CONCAT('%', :search, '%')) 
                   OR LOWER(m.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) 
                AND m.deletedAt IS NULL
                ORDER BY m.firstName, m.lastName
            """)
    List<Member> searchByName(@Param("search") String search);

    boolean existsByPhoneAndDeletedAtIsNull(String phone);

    boolean existsByUser_IdAndDeletedAtIsNull(Long userId);
}