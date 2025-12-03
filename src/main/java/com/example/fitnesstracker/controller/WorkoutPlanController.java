package com.example.fitnesstracker.controller;

import com.example.fitnesstracker.dto.request.workout.*;
import com.example.fitnesstracker.dto.response.workout.*;
import com.example.fitnesstracker.service.WorkoutPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/workout-plans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Workout Plans", description = "Gestión de planes de entrenamiento")
@SecurityRequirement(name = "bearerAuth")
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    // Trainer crea y gestiona planes

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Crear plan de workout", description = "Trainer crea un plan para un miembro")
    public ResponseEntity<WorkoutPlanDTO> createWorkoutPlan(
            @RequestParam Long trainerId,
            @Valid @RequestBody CreateWorkoutPlanDTO dto) {
        log.info("POST /api/workout-plans - Trainer {} creando plan", trainerId);
        WorkoutPlanDTO plan = workoutPlanService.createWorkoutPlan(trainerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    @PostMapping("/{planId}/days")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Agregar día al plan", description = "Trainer agrega un día de entrenamiento al plan")
    public ResponseEntity<WorkoutDayDTO> addWorkoutDay(
            @PathVariable Long planId,
            @RequestParam Long trainerId,
            @Valid @RequestBody AddWorkoutDayDTO dto) {
        log.info("POST /api/workout-plans/{}/days - Trainer {}", planId, trainerId);
        WorkoutDayDTO day = workoutPlanService.addWorkoutDay(planId, trainerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(day);
    }

    @PostMapping("/days/{dayId}/exercises")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Agregar ejercicio al día", description = "Trainer agrega un ejercicio a un día específico")
    public ResponseEntity<WorkoutExerciseDTO> addExerciseToDay(
            @PathVariable Long dayId,
            @RequestParam Long trainerId,
            @Valid @RequestBody AddExerciseToWorkoutDTO dto) {
        log.info("POST /api/workout-plans/days/{}/exercises - Trainer {}", dayId, trainerId);
        WorkoutExerciseDTO exercise = workoutPlanService.addExerciseToDay(dayId, trainerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(exercise);
    }

    @PostMapping("/{planId}/activate")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Activar plan", description = "Trainer activa el plan para que el miembro pueda usarlo")
    public ResponseEntity<WorkoutPlanDTO> activateWorkoutPlan(
            @PathVariable Long planId,
            @RequestParam Long trainerId) {
        log.info("POST /api/workout-plans/{}/activate - Trainer {}", planId, trainerId);
        WorkoutPlanDTO plan = workoutPlanService.activateWorkoutPlan(planId, trainerId);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Obtener plan por ID", description = "Obtiene detalle completo del plan con días y ejercicios")
    public ResponseEntity<WorkoutPlanDTO> getWorkoutPlanById(@PathVariable Long planId) {
        log.info("GET /api/workout-plans/{}", planId);
        WorkoutPlanDTO plan = workoutPlanService.getWorkoutPlanById(planId);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/trainers/{trainerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Planes del trainer", description = "Obtiene todos los planes creados por un trainer")
    public ResponseEntity<List<WorkoutPlanDTO>> getWorkoutPlansByTrainer(@PathVariable Long trainerId) {
        log.info("GET /api/workout-plans/trainers/{}", trainerId);
        List<WorkoutPlanDTO> plans = workoutPlanService.getWorkoutPlansByTrainer(trainerId);
        return ResponseEntity.ok(plans);
    }

    @DeleteMapping("/{planId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Eliminar plan", description = "Trainer elimina un plan de workout")
    public ResponseEntity<Void> deleteWorkoutPlan(
            @PathVariable Long planId,
            @RequestParam Long trainerId) {
        log.info("DELETE /api/workout-plans/{} - Trainer {}", planId, trainerId);
        workoutPlanService.deleteWorkoutPlan(planId, trainerId);
        return ResponseEntity.noContent().build();
    }

    // Member ve y completa workouts

    @GetMapping("/members/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Planes del miembro", description = "Obtiene todos los planes de un miembro")
    public ResponseEntity<List<WorkoutPlanDTO>> getWorkoutPlansByMember(@PathVariable Long memberId) {
        log.info("GET /api/workout-plans/members/{}", memberId);
        List<WorkoutPlanDTO> plans = workoutPlanService.getWorkoutPlansByMember(memberId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/members/{memberId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Planes activos del miembro", description = "Obtiene planes activos con detalle completo")
    public ResponseEntity<List<WorkoutPlanDTO>> getActiveWorkoutPlansByMember(@PathVariable Long memberId) {
        log.info("GET /api/workout-plans/members/{}/active", memberId);
        List<WorkoutPlanDTO> plans = workoutPlanService.getActiveWorkoutPlansByMember(memberId);
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/days/{dayId}/complete")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Completar workout", description = "Member marca un día como completado y registra resultados")
    public ResponseEntity<WorkoutCompletionDTO> completeWorkout(
            @PathVariable Long dayId,
            @RequestParam Long memberId,
            @Valid @RequestBody CompleteWorkoutDTO dto) {
        log.info("POST /api/workout-plans/days/{}/complete - Member {}", dayId, memberId);
        WorkoutCompletionDTO completion = workoutPlanService.completeWorkout(memberId, dayId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(completion);
    }

    @GetMapping("/members/{memberId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Historial de workouts", description = "Obtiene historial completo de workouts completados")
    public ResponseEntity<List<WorkoutCompletionDTO>> getWorkoutHistory(@PathVariable Long memberId) {
        log.info("GET /api/workout-plans/members/{}/history", memberId);
        List<WorkoutCompletionDTO> history = workoutPlanService.getWorkoutHistory(memberId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/members/{memberId}/history/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Historial por rango de fechas", description = "Obtiene workouts completados en un período")
    public ResponseEntity<List<WorkoutCompletionDTO>> getWorkoutHistoryByDateRange(
            @PathVariable Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/workout-plans/members/{}/history/range?startDate={}&endDate={}", memberId, startDate, endDate);
        List<WorkoutCompletionDTO> history = workoutPlanService.getWorkoutHistoryByDateRange(memberId, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    // Estadísticas

    @GetMapping("/members/{memberId}/stats/completed-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Contar workouts completados", description = "Total de workouts completados por el miembro")
    public ResponseEntity<Long> countCompletedWorkouts(@PathVariable Long memberId) {
        log.info("GET /api/workout-plans/members/{}/stats/completed-count", memberId);
        long count = workoutPlanService.countCompletedWorkouts(memberId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/trainers/{trainerId}/stats/active-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Contar planes activos", description = "Total de planes activos del trainer")
    public ResponseEntity<Long> countActivePlans(@PathVariable Long trainerId) {
        log.info("GET /api/workout-plans/trainers/{}/stats/active-count", trainerId);
        long count = workoutPlanService.countActivePlans(trainerId);
        return ResponseEntity.ok(count);
    }
}
