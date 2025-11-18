package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.enums.DifficultyLevel;
import com.example.fitnesstracker.enums.ExerciseCategory;
import com.example.fitnesstracker.enums.MuscleGroup;
import com.example.fitnesstracker.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // Buscar por categoría
    List<Exercise> findByCategoryAndDeletedAtIsNull(ExerciseCategory category);

    // Buscar por grupo muscular
    List<Exercise> findByPrimaryMuscleAndDeletedAtIsNull(MuscleGroup muscleGroup);

    // Buscar por dificultad
    List<Exercise> findByDifficultyAndDeletedAtIsNull(DifficultyLevel difficulty);

    // Buscar ejercicios del catálogo (no custom)
    @Query("SELECT e FROM Exercise e WHERE e.isCustom = false AND e.deletedAt IS NULL ORDER BY e.name ASC")
    List<Exercise> findCatalogExercises();

    // Buscar ejercicios custom de un trainer
    @Query("SELECT e FROM Exercise e WHERE e.isCustom = true AND e.createdByTrainer.id = :trainerId AND e.deletedAt IS NULL ORDER BY e.createdAt DESC")
    List<Exercise> findCustomExercisesByTrainer(@Param("trainerId") Long trainerId);

    // Buscar por nombre
    @Query("SELECT e FROM Exercise e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')) AND e.deletedAt IS NULL")
    List<Exercise> searchByName(@Param("name") String name);

    // Buscar por múltiples filtros
    @Query("SELECT e FROM Exercise e WHERE e.category = :category AND e.primaryMuscle = :muscle AND e.difficulty = :difficulty AND e.isCustom = false AND e.deletedAt IS NULL")
    List<Exercise> findByFilters(@Param("category") ExerciseCategory category, @Param("muscle") MuscleGroup muscle, @Param("difficulty") DifficultyLevel difficulty);

    // Contar ejercicios del catálogo
    @Query("SELECT COUNT(e) FROM Exercise e WHERE e.isCustom = false AND e.deletedAt IS NULL")
    long countCatalogExercises();

    // Contar ejercicios custom de un trainer
    @Query("SELECT COUNT(e) FROM Exercise e WHERE e.isCustom = true AND e.createdByTrainer.id = :trainerId AND e.deletedAt IS NULL")
    long countCustomExercisesByTrainer(@Param("trainerId") Long trainerId);

    // Verificar si ya existe un ejercicio con ese nombre (para evitar duplicados)
    @Query("SELECT COUNT(e) > 0 FROM Exercise e WHERE LOWER(e.name) = LOWER(:name) AND e.isCustom = false AND e.deletedAt IS NULL")
    boolean existsCatalogExerciseByName(@Param("name") String name);
}