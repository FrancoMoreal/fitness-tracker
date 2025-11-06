package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUser_Id(Long userId);

    Optional<Trainer> findByExternalId(String externalId);

    /**
     * Trae Trainer + User en una sola query
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT t FROM Trainer t WHERE t.user.id = :userId AND t.deletedAt IS NULL")
    Optional<Trainer> findByUserIdWithUser(@Param("userId") Long userId);

    @Query("""
                SELECT DISTINCT t FROM Trainer t
                LEFT JOIN FETCH t.user u
                LEFT JOIN FETCH t.assignedMembers m
                WHERE t.id = :id
                AND t.deletedAt IS NULL
            """)
    Optional<Trainer> findByIdWithFullProfile(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
                SELECT t FROM Trainer t
                WHERE t.deletedAt IS NULL
                ORDER BY t.firstName, t.lastName
            """)
    List<Trainer> findAllActiveWithUser();

    @Deprecated
    @Query("SELECT t FROM Trainer t WHERE t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Trainer> findAllActiveTrainers();

    List<Trainer> findBySpecialtyContainingIgnoreCase(String specialty);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
                SELECT t FROM Trainer t 
                WHERE LOWER(t.specialty) LIKE LOWER(CONCAT('%', :specialty, '%')) 
                AND t.deletedAt IS NULL
                ORDER BY t.firstName, t.lastName
            """)
    List<Trainer> findActiveTrainersBySpecialty(@Param("specialty") String specialty);

    @Query("""
                SELECT t.id as trainerId,
                       t.firstName as firstName,
                       t.lastName as lastName,
                       t.specialty as specialty,
                       COUNT(m.id) as memberCount
                FROM Trainer t
                LEFT JOIN t.assignedMembers m ON m.deletedAt IS NULL
                WHERE t.deletedAt IS NULL
                GROUP BY t.id, t.firstName, t.lastName, t.specialty
                ORDER BY memberCount DESC, t.firstName
            """)
    List<TrainerStatProjection> findTrainersWithMemberCount();

    @Deprecated
    @Query("""
                SELECT t FROM Trainer t 
                WHERE t.deletedAt IS NULL 
                ORDER BY SIZE(t.assignedMembers) DESC
            """)
    List<Trainer> findMostBusyTrainers();

    @Query("""
                SELECT t FROM Trainer t
                WHERE t.deletedAt IS NULL
                AND (SELECT COUNT(m) FROM Member m 
                     WHERE m.assignedTrainer = t 
                     AND m.deletedAt IS NULL) < :maxCapacity
                ORDER BY t.firstName, t.lastName
            """)
    List<Trainer> findTrainersWithAvailability(@Param("maxCapacity") int maxCapacity);

    @Deprecated
    @Query("""
                SELECT t FROM Trainer t 
                WHERE SIZE(t.assignedMembers) = 0 
                AND t.deletedAt IS NULL
            """)
    List<Trainer> findAvailableTrainersWithNoMembers();

    @Query("""
                SELECT t FROM Trainer t 
                WHERE (LOWER(t.firstName) LIKE LOWER(CONCAT('%', :search, '%')) 
                   OR LOWER(t.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) 
                AND t.deletedAt IS NULL
                ORDER BY t.firstName, t.lastName
            """)
    List<Trainer> searchByName(@Param("search") String search);

    boolean existsByUser_IdAndDeletedAtIsNull(Long userId);

    @Query("SELECT COUNT(t) FROM Trainer t WHERE t.deletedAt IS NULL")
    long countActiveTrainers();

    @Query("SELECT COUNT(t) FROM Trainer t WHERE t.deletedAt IS NULL")
    long countAllTrainers();

    @Query("""
                SELECT t FROM Trainer t
                LEFT JOIN t.assignedMembers m ON m.deletedAt IS NULL
                WHERE t.specialty = :specialty 
                AND t.deletedAt IS NULL
                GROUP BY t
                ORDER BY COUNT(m) ASC, t.firstName
            """)
    List<Trainer> findTrainersBySpecialtyOrderedByLoad(@Param("specialty") String specialty);
}