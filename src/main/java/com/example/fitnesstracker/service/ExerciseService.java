package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.exercise.CreateExerciseDTO;
import com.example.fitnesstracker.dto.request.exercise.UpdateExerciseDTO;
import com.example.fitnesstracker.dto.response.exercise.ExerciseDTO;
import com.example.fitnesstracker.enums.DifficultyLevel;
import com.example.fitnesstracker.enums.ExerciseCategory;
import com.example.fitnesstracker.enums.MuscleGroup;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.ResourceNotFoundException;
import com.example.fitnesstracker.exception.UserAlreadyExistsException;
import com.example.fitnesstracker.mapper.ExerciseMapper;
import com.example.fitnesstracker.model.Exercise;
import com.example.fitnesstracker.model.Trainer;
import com.example.fitnesstracker.repository.ExerciseRepository;
import com.example.fitnesstracker.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final TrainerRepository trainerRepository;
    private final ExerciseMapper exerciseMapper;

    @Transactional
    public ExerciseDTO createCatalogExercise(CreateExerciseDTO dto) {
        log.info("Creando ejercicio en catálogo: {}", dto.getName());

        // Validar que no exista en el catálogo
        if (exerciseRepository.existsCatalogExerciseByName(dto.getName())) {
            throw new UserAlreadyExistsException("exercise", dto.getName());
        }

        Exercise exercise = exerciseMapper.toEntity(dto);
        exercise.setIsCustom(false);
        exercise.setCreatedByTrainer(null);

        Exercise savedExercise = exerciseRepository.save(exercise);
        log.info("Ejercicio creado en catálogo: {} (ID: {})", savedExercise.getName(), savedExercise.getId());

        return exerciseMapper.toDTO(savedExercise);
    }

    @Transactional
    public ExerciseDTO createCustomExercise(Long trainerId, CreateExerciseDTO dto) {
        log.info("Trainer {} creando ejercicio custom: {}", trainerId, dto.getName());

        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrenador no encontrado"));

        if (trainer.isDeleted() || !trainer.getIsActive()) {
            throw new ResourceNotFoundException("Entrenador no disponible");
        }

        Exercise exercise = exerciseMapper.toEntity(dto);
        exercise.setIsCustom(true);
        exercise.setCreatedByTrainer(trainer);

        Exercise savedExercise = exerciseRepository.save(exercise);
        log.info("Ejercicio custom creado: {} (ID: {})", savedExercise.getName(), savedExercise.getId());

        return exerciseMapper.toDTO(savedExercise);
    }

    public List<ExerciseDTO> getAllCatalogExercises() {
        log.debug("Obteniendo todos los ejercicios del catálogo");
        return exerciseRepository.findCatalogExercises().stream()
                .map(exerciseMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExerciseDTO> getCustomExercisesByTrainer(Long trainerId) {
        log.debug("Obteniendo ejercicios custom del trainer: {}", trainerId);
        return exerciseRepository.findCustomExercisesByTrainer(trainerId).stream()
                .map(exerciseMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExerciseDTO> getExercisesByCategory(ExerciseCategory category) {
        log.debug("Obteniendo ejercicios por categoría: {}", category);
        return exerciseRepository.findByCategoryAndDeletedAtIsNull(category).stream()
                .map(exerciseMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExerciseDTO> getExercisesByMuscle(MuscleGroup muscle) {
        log.debug("Obteniendo ejercicios por grupo muscular: {}", muscle);
        return exerciseRepository.findByPrimaryMuscleAndDeletedAtIsNull(muscle).stream()
                .map(exerciseMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExerciseDTO> getExercisesByDifficulty(DifficultyLevel difficulty) {
        log.debug("Obteniendo ejercicios por dificultad: {}", difficulty);
        return exerciseRepository.findByDifficultyAndDeletedAtIsNull(difficulty).stream()
                .map(exerciseMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExerciseDTO> searchExercises(String name) {
        log.debug("Buscando ejercicios por nombre: {}", name);
        return exerciseRepository.searchByName(name).stream()
                .map(exerciseMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ExerciseDTO getExerciseById(Long exerciseId) {
        log.debug("Obteniendo ejercicio por ID: {}", exerciseId);
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

        if (exercise.isDeleted()) {
            throw new ResourceNotFoundException("Ejercicio no encontrado");
        }

        return exerciseMapper.toDTO(exercise);
    }

    @Transactional
    public ExerciseDTO updateExercise(Long exerciseId, UpdateExerciseDTO dto, Long trainerId) {
        log.info("Actualizando ejercicio: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

        if (exercise.isDeleted()) {
            throw new ResourceNotFoundException("Ejercicio no encontrado");
        }

        // Si es custom, verificar que sea del trainer
        if (exercise.getIsCustom() && !exercise.getCreatedByTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("No tienes permiso para modificar este ejercicio");
        }

        // Si es del catálogo, solo ADMIN puede modificar (esto se valida en el controller)

        exerciseMapper.updateFromDTO(dto, exercise);
        Exercise updatedExercise = exerciseRepository.save(exercise);

        log.info("Ejercicio actualizado: {}", exerciseId);
        return exerciseMapper.toDTO(updatedExercise);
    }

    @Transactional
    public void deleteExercise(Long exerciseId, Long trainerId) {
        log.info("Eliminando ejercicio: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

        // Si es custom, verificar que sea del trainer
        if (exercise.getIsCustom() && !exercise.getCreatedByTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("No tienes permiso para eliminar este ejercicio");
        }

        exercise.softDelete();
        exerciseRepository.save(exercise);

        log.info("Ejercicio eliminado: {}", exerciseId);
    }

    public long countCatalogExercises() {
        return exerciseRepository.countCatalogExercises();
    }

    public long countCustomExercisesByTrainer(Long trainerId) {
        return exerciseRepository.countCustomExercisesByTrainer(trainerId);
    }
}