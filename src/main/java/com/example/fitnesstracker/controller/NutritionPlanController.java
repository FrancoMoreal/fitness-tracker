package com.example.fitnesstracker.controller;

import com.example.fitnesstracker.dto.request.nutrition.AddNutritionMealDTO;
import com.example.fitnesstracker.dto.request.nutrition.CreateNutritionPlanDTO;
import com.example.fitnesstracker.dto.response.nutrition.NutritionMealDTO;
import com.example.fitnesstracker.dto.response.nutrition.NutritionPlanDTO;
import com.example.fitnesstracker.service.NutritionPlanService;
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
@RequestMapping("/api/nutrition-plans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Nutrition Plans", description = "Gestión de planes nutricionales")
@SecurityRequirement(name = "bearerAuth")
public class NutritionPlanController {

    private final NutritionPlanService nutritionPlanService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Crear plan nutricional", description = "Trainer crea un plan para un miembro")
    public ResponseEntity<NutritionPlanDTO> createPlan(
            @RequestParam Long trainerId,
            @Valid @RequestBody CreateNutritionPlanDTO dto) {
        log.info("POST /api/nutrition-plans - Trainer {}", trainerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nutritionPlanService.createNutritionPlan(trainerId, dto));
    }

    @PostMapping("/{planId}/meals")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Agregar comida al plan", description = "Trainer agrega una comida al plan nutricional")
    public ResponseEntity<NutritionMealDTO> addMeal(
            @PathVariable Long planId,
            @RequestParam Long trainerId,
            @Valid @RequestBody AddNutritionMealDTO dto) {
        log.info("POST /api/nutrition-plans/{}/meals - Trainer {}", planId, trainerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nutritionPlanService.addMeal(planId, trainerId, dto));
    }

    @PostMapping("/{planId}/activate")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Activar plan", description = "Trainer activa el plan para que el miembro pueda verlo")
    public ResponseEntity<NutritionPlanDTO> activatePlan(
            @PathVariable Long planId,
            @RequestParam Long trainerId) {
        log.info("POST /api/nutrition-plans/{}/activate - Trainer {}", planId, trainerId);
        return ResponseEntity.ok(nutritionPlanService.activatePlan(planId, trainerId));
    }

    @GetMapping("/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Obtener plan por ID", description = "Detalle completo del plan con comidas")
    public ResponseEntity<NutritionPlanDTO> getPlanById(@PathVariable Long planId) {
        log.info("GET /api/nutrition-plans/{}", planId);
        return ResponseEntity.ok(nutritionPlanService.getPlanById(planId));
    }

    @GetMapping("/members/{memberId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Planes activos del miembro", description = "Obtiene planes activos con detalle completo")
    public ResponseEntity<List<NutritionPlanDTO>> getActivePlansByMember(@PathVariable Long memberId) {
        log.info("GET /api/nutrition-plans/members/{}/active", memberId);
        return ResponseEntity.ok(nutritionPlanService.getActivePlansByMember(memberId));
    }

    @GetMapping("/members/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Todos los planes del miembro", description = "Historial completo de planes")
    public ResponseEntity<List<NutritionPlanDTO>> getAllPlansByMember(@PathVariable Long memberId) {
        log.info("GET /api/nutrition-plans/members/{}", memberId);
        return ResponseEntity.ok(nutritionPlanService.getAllPlansByMember(memberId));
    }

    @GetMapping("/trainers/{trainerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Planes del trainer", description = "Todos los planes creados por el trainer")
    public ResponseEntity<List<NutritionPlanDTO>> getPlansByTrainer(@PathVariable Long trainerId) {
        log.info("GET /api/nutrition-plans/trainers/{}", trainerId);
        return ResponseEntity.ok(nutritionPlanService.getPlansByTrainer(trainerId));
    }

    @DeleteMapping("/{planId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Eliminar plan", description = "Trainer elimina un plan nutricional")
    public ResponseEntity<Void> deletePlan(
            @PathVariable Long planId,
            @RequestParam Long trainerId) {
        log.info("DELETE /api/nutrition-plans/{} - Trainer {}", planId, trainerId);
        nutritionPlanService.deletePlan(planId, trainerId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/meals/{mealId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Eliminar comida", description = "Trainer elimina una comida del plan")
    public ResponseEntity<Void> removeMeal(
            @PathVariable Long mealId,
            @RequestParam Long trainerId) {
        log.info("DELETE /api/nutrition-plans/meals/{} - Trainer {}", mealId, trainerId);
        nutritionPlanService.removeMeal(mealId, trainerId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{planId}/cancel")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Cancelar plan", description = "Trainer cancela un plan nutricional")
    public ResponseEntity<Void> cancelPlan(
            @PathVariable Long planId,
            @RequestParam Long trainerId) {
        log.info("POST /api/nutrition-plans/{}/cancel - Trainer {}", planId, trainerId);
        nutritionPlanService.cancelPlan(planId, trainerId);
        return ResponseEntity.noContent().build();
    }
}