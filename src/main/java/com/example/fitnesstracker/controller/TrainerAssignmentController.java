package com.example.fitnesstracker.controller;

import com.example.fitnesstracker.dto.request.assignment.RequestTrainerDTO;
import com.example.fitnesstracker.dto.request.assignment.RespondRequestDTO;
import com.example.fitnesstracker.dto.response.assignment.TrainerAssignmentRequestDTO;
import com.example.fitnesstracker.service.TrainerAssignmentService;
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
@RequestMapping("/api/trainer-assignments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trainer Assignments", description = "Gestión de asignación de entrenadores a miembros")
@SecurityRequirement(name = "bearerAuth")
public class TrainerAssignmentController {

    private final TrainerAssignmentService assignmentService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Solicitar entrenador", description = "Member solicita ser asignado a un trainer")
    public ResponseEntity<TrainerAssignmentRequestDTO> requestTrainer(
            @RequestParam Long memberId,
            @Valid @RequestBody RequestTrainerDTO dto) {
        log.info("POST /api/trainer-assignments/request - Member: {}, Trainer: {}", memberId, dto.getTrainerId());
        TrainerAssignmentRequestDTO request = assignmentService.requestTrainer(memberId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    @PostMapping("/{requestId}/accept")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Aceptar solicitud", description = "Trainer acepta solicitud de un member")
    public ResponseEntity<TrainerAssignmentRequestDTO> acceptRequest(
            @PathVariable Long requestId,
            @RequestParam Long trainerId,
            @Valid @RequestBody RespondRequestDTO dto) {
        log.info("POST /api/trainer-assignments/{}/accept - Trainer: {}", requestId, trainerId);
        TrainerAssignmentRequestDTO request = assignmentService.acceptRequest(requestId, trainerId, dto);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Rechazar solicitud", description = "Trainer rechaza solicitud de un member")
    public ResponseEntity<TrainerAssignmentRequestDTO> rejectRequest(
            @PathVariable Long requestId,
            @RequestParam Long trainerId,
            @Valid @RequestBody RespondRequestDTO dto) {
        log.info("POST /api/trainer-assignments/{}/reject - Trainer: {}", requestId, trainerId);
        TrainerAssignmentRequestDTO request = assignmentService.rejectRequest(requestId, trainerId, dto);
        return ResponseEntity.ok(request);
    }

    @DeleteMapping("/{requestId}/cancel")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Cancelar solicitud", description = "Member cancela su solicitud pendiente")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable Long requestId,
            @RequestParam Long memberId) {
        log.info("DELETE /api/trainer-assignments/{}/cancel - Member: {}", requestId, memberId);
        assignmentService.cancelRequest(requestId, memberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/members/{memberId}/remove-trainer")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Remover entrenador", description = "Member remueve su trainer actual")
    public ResponseEntity<Void> removeTrainer(@PathVariable Long memberId) {
        log.info("DELETE /api/trainer-assignments/members/{}/remove-trainer", memberId);
        assignmentService.removeTrainer(memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trainers/{trainerId}/pending")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Solicitudes pendientes", description = "Obtiene solicitudes pendientes del trainer")
    public ResponseEntity<List<TrainerAssignmentRequestDTO>> getPendingRequests(@PathVariable Long trainerId) {
        log.info("GET /api/trainer-assignments/trainers/{}/pending", trainerId);
        List<TrainerAssignmentRequestDTO> requests = assignmentService.getPendingRequestsForTrainer(trainerId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/trainers/{trainerId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Todas las solicitudes del trainer", description = "Obtiene historial completo de solicitudes")
    public ResponseEntity<List<TrainerAssignmentRequestDTO>> getAllTrainerRequests(@PathVariable Long trainerId) {
        log.info("GET /api/trainer-assignments/trainers/{}", trainerId);
        List<TrainerAssignmentRequestDTO> requests = assignmentService.getAllRequestsForTrainer(trainerId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/members/{memberId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Solicitudes del member", description = "Obtiene historial de solicitudes del member")
    public ResponseEntity<List<TrainerAssignmentRequestDTO>> getMemberRequests(@PathVariable Long memberId) {
        log.info("GET /api/trainer-assignments/members/{}", memberId);
        List<TrainerAssignmentRequestDTO> requests = assignmentService.getRequestsForMember(memberId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Detalle de solicitud", description = "Obtiene detalle de una solicitud específica")
    public ResponseEntity<TrainerAssignmentRequestDTO> getRequestById(@PathVariable Long requestId) {
        log.info("GET /api/trainer-assignments/{}", requestId);
        TrainerAssignmentRequestDTO request = assignmentService.getRequestById(requestId);
        return ResponseEntity.ok(request);
    }

    @GetMapping("/trainers/{trainerId}/pending-count")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Contar pendientes", description = "Cuenta solicitudes pendientes del trainer")
    public ResponseEntity<Long> countPendingRequests(@PathVariable Long trainerId) {
        log.info("GET /api/trainer-assignments/trainers/{}/pending-count", trainerId);
        long count = assignmentService.countPendingRequestsForTrainer(trainerId);
        return ResponseEntity.ok(count);
    }
}