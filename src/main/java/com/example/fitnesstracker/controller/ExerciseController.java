package com.example.fitnesstracker.controller;

import com.example.fitnesstracker.dto.request.exercise.CreateExerciseDTO;
import com.example.fitnesstracker.dto.request.exercise.UpdateExerciseDTO;
import com.example.fitnesstracker.dto.response.exercise.ExerciseDTO;
import com.example.fitnesstracker.enums.DifficultyLevel;
import com.example.fitnesstracker.enums.ExerciseCategory;
import com.example.fitnesstracker.enums.MuscleGroup;
import com.example.fitnesstracker.service.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Exercises", description = "Catálogo de ejercicios")
@SecurityRequirement(name = "bearerAuth")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping("/catalog")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Listar catálogo de ejercicios", description = "Obtiene todos los ejercicios del catálogo general")
    public ResponseEntity<List<ExerciseDTO>> getCatalogExercises() {
        log.info("GET /api/exercises/catalog - Listando catálogo");
        List<ExerciseDTO> exercises = exerciseService.getAllCatalogExercises();
        return ResponseEntity.ok(exercises);
    }

    @PostMapping("/catalog")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear ejercicio en catálogo", description = "Admin crea ejercicio en el catálogo general")
    public ResponseEntity<ExerciseDTO> createCatalogExercise(@Valid @RequestBody CreateExerciseDTO dto) {
        log.info("POST /api/exercises/catalog - Creando ejercicio: {}", dto.getName());
        ExerciseDTO exercise = exerciseService.createCatalogExercise(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(exercise);
    }

    @PostMapping("/custom")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Crear ejercicio custom", description = "Trainer crea ejercicio personalizado")
    public ResponseEntity<ExerciseDTO> createCustomExercise(
            @RequestParam Long trainerId,
            @Valid @RequestBody CreateExerciseDTO dto) {
        log.info("POST /api/exercises/custom - Trainer {} creando ejercicio", trainerId);
        ExerciseDTO exercise = exerciseService.createCustomExercise(trainerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(exercise);
    }

    @GetMapping("/trainers/{trainerId}/custom")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Ejercicios custom del trainer", description = "Obtiene ejercicios personalizados de un trainer")
    public ResponseEntity<List<ExerciseDTO>> getCustomExercisesByTrainer(@PathVariable Long trainerId) {
        log.info("GET /api/exercises/trainers/{}/custom", trainerId);
        List<ExerciseDTO> exercises = exerciseService.getCustomExercisesByTrainer(trainerId);
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Ejercicios por categoría", description = "Filtra ejercicios por categoría")
    public ResponseEntity<List<ExerciseDTO>> getExercisesByCategory(@PathVariable ExerciseCategory category) {
        log.info("GET /api/exercises/category/{}", category);
        List<ExerciseDTO> exercises = exerciseService.getExercisesByCategory(category);
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/muscle/{muscle}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Ejercicios por grupo muscular", description = "Filtra ejercicios por músculo")
    public ResponseEntity<List<ExerciseDTO>> getExercisesByMuscle(@PathVariable MuscleGroup muscle) {
        log.info("GET /api/exercises/muscle/{}", muscle);
        List<ExerciseDTO> exercises = exerciseService.getExercisesByMuscle(muscle);
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/difficulty/{difficulty}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Ejercicios por dificultad", description = "Filtra ejercicios por nivel")
    public ResponseEntity<List<ExerciseDTO>> getExercisesByDifficulty(@PathVariable DifficultyLevel difficulty) {
        log.info("GET /api/exercises/difficulty/{}", difficulty);
        List<ExerciseDTO> exercises = exerciseService.getExercisesByDifficulty(difficulty);
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Buscar ejercicios", description = "Busca ejercicios por nombre")
    public ResponseEntity<List<ExerciseDTO>> searchExercises(@RequestParam String name) {
        log.info("GET /api/exercises/search?name={}", name);
        List<ExerciseDTO> exercises = exerciseService.searchExercises(name);
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Obtener ejercicio por ID", description = "Obtiene detalle de un ejercicio")
    public ResponseEntity<ExerciseDTO> getExerciseById(@PathVariable Long id) {
        log.info("GET /api/exercises/{}", id);
        ExerciseDTO exercise = exerciseService.getExerciseById(id);
        return ResponseEntity.ok(exercise);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Actualizar ejercicio", description = "Actualiza ejercicio (Admin: cualquiera, Trainer: solo custom propios)")
    public ResponseEntity<ExerciseDTO> updateExercise(
            @PathVariable Long id,
            @RequestParam Long trainerId,
            @Valid @RequestBody UpdateExerciseDTO dto) {
        log.info("PUT /api/exercises/{} - Trainer: {}", id, trainerId);
        ExerciseDTO exercise = exerciseService.updateExercise(id, dto, trainerId);
        return ResponseEntity.ok(exercise);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Eliminar ejercicio", description = "Elimina ejercicio (Admin: cualquiera, Trainer: solo custom propios)")
    public ResponseEntity<Void> deleteExercise(
            @PathVariable Long id,
            @RequestParam Long trainerId) {
        log.info("DELETE /api/exercises/{} - Trainer: {}", id, trainerId);
        exerciseService.deleteExercise(id, trainerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/catalog/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Contar ejercicios del catálogo", description = "Retorna total de ejercicios en catálogo")
    public ResponseEntity<Long> countCatalogExercises() {
        log.info("GET /api/exercises/catalog/count");
        long count = exerciseService.countCatalogExercises();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/trainers/{trainerId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Contar ejercicios custom del trainer", description = "Retorna total de ejercicios custom")
    public ResponseEntity<Long> countCustomExercises(@PathVariable Long trainerId) {
        log.info("GET /api/exercises/trainers/{}/count", trainerId);
        long count = exerciseService.countCustomExercisesByTrainer(trainerId);
        return ResponseEntity.ok(count);
    }
}