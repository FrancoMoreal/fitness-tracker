package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    // Búsquedas básicas
    Optional<Trainer> findByUser_Id(Long userId);
    Optional<Trainer> findByExternalId(String externalId);

    // Búsquedas por estado
    List<Trainer> findByIsActive(Boolean isActive);
    List<Trainer> findByIsActiveAndDeletedAtIsNull(Boolean isActive);

    // Búsquedas por especialidad
    List<Trainer> findBySpecialtyContainingIgnoreCase(String specialty);

    @Query("SELECT t FROM Trainer t WHERE LOWER(t.specialty) LIKE LOWER(CONCAT('%', :specialty, '%')) AND t.isActive = true AND t.deletedAt IS NULL")
    List<Trainer> findActiveTrainersBySpecialty(@Param("specialty") String specialty);

    // Búsquedas avanzadas
    @Query("SELECT t FROM Trainer t WHERE t.isActive = true AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Trainer> findAllActiveTrainers();

    @Query("SELECT t FROM Trainer t WHERE t.isActive = true AND t.deletedAt IS NULL ORDER BY SIZE(t.assignedMembers) DESC")
    List<Trainer> findMostBusyTrainers();

    @Query("SELECT t FROM Trainer t WHERE SIZE(t.assignedMembers) = 0 AND t.isActive = true AND t.deletedAt IS NULL")
    List<Trainer> findAvailableTrainersWithNoMembers();

    // Búsqueda por nombre
    @Query("SELECT t FROM Trainer t WHERE (LOWER(t.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) AND t.deletedAt IS NULL")
    List<Trainer> searchByName(@Param("search") String search);

    // Existencias
    boolean existsByUser_IdAndDeletedAtIsNull(Long userId);

    // Contar trainers
    @Query("SELECT COUNT(t) FROM Trainer t WHERE t.isActive = true AND t.deletedAt IS NULL")
    long countActiveTrainers();

    @Query("SELECT COUNT(t) FROM Trainer t WHERE t.deletedAt IS NULL")
    long countAllTrainers();

    // Trainers ordenados por carga de trabajo
    @Query("SELECT t FROM Trainer t WHERE t.specialty = :specialty AND t.isActive = true AND t.deletedAt IS NULL ORDER BY SIZE(t.assignedMembers) ASC")
    List<Trainer> findTrainersBySpecialtyOrderedByLoad(@Param("specialty") String specialty);
}
