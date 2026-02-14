// java
package com.example.fitnesstracker.controller;

import com.example.fitnesstracker.dto.request.trainer.RegisterTrainerDTO;
import com.example.fitnesstracker.dto.request.trainer.UpdateTrainerDTO;
import com.example.fitnesstracker.dto.response.TrainerDTO;
import com.example.fitnesstracker.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trainers", description = "Gestión de entrenadores")
// @SecurityRequirement(name = "bearerAuth")
public class TrainerController {

    private final TrainerService trainerService;

    @GetMapping
    // @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Listar todos los entrenadores", description = "Obtiene lista de entrenadores activos")
    public ResponseEntity<List<TrainerDTO>> getAllTrainers() {
        log.info("GET /api/trainers - Listando entrenadores");
        return ResponseEntity.ok(trainerService.getAllTrainers());
    }

    @GetMapping("/{id}")
    // @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Obtener entrenador por ID", description = "Busca un entrenador por su ID")
    public ResponseEntity<TrainerDTO> getTrainerById(@PathVariable Long id) {
        log.info("GET /api/trainers/{} - Obteniendo entrenador", id);
        return ResponseEntity.ok(trainerService.getTrainerById(id));
    }

    @GetMapping("/external/{externalId}")
    // @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Obtener entrenador por External ID", description = "Busca un entrenador por su UUID externo")
    public ResponseEntity<TrainerDTO> getTrainerByExternalId(@PathVariable String externalId) {
        log.info("GET /api/trainers/external/{} - Obteniendo entrenador", externalId);
        return ResponseEntity.ok(trainerService.getTrainerByExternalId(externalId));
    }

    @GetMapping("/available")
    //  @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Listar entrenadores disponibles", description = "Obtiene entrenadores sin miembros asignados")
    public ResponseEntity<List<TrainerDTO>> getAvailableTrainers() {
        log.info("GET /api/trainers/available - Obteniendo trainers disponibles");
        return ResponseEntity.ok(trainerService.getAvailableTrainers());
    }

    @GetMapping("/specialty/{specialty}")
    // @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Buscar por especialidad", description = "Busca entrenadores por especialidad")
    public ResponseEntity<List<TrainerDTO>> searchTrainersBySpecialty(@PathVariable String specialty) {
        log.info("GET /api/trainers/specialty/{} - Buscando por especialidad", specialty);
        return ResponseEntity.ok(trainerService.searchTrainersBySpecialty(specialty));
    }

    @GetMapping("/most-busy")
    //  @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Entrenadores más ocupados", description = "Obtiene entrenadores ordenados por carga de trabajo")
    public ResponseEntity<List<TrainerDTO>> getMostBusyTrainers() {
        log.info("GET /api/trainers/most-busy - Obteniendo trainers más ocupados");
        return ResponseEntity.ok(trainerService.getMostBusyTrainers());
    }

    //   @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    // @Operation(summary = "Crear entrenador", description = "Crea un nuevo entrenador y su usuario asociado")
    // public ResponseEntity<TrainerDTO> createTrainer(@Valid @RequestBody RegisterTrainerDTO dto) {
    //     log.info("POST /api/trainers - Creando entrenador: {}", dto.getUsername());
    //    TrainerDTO created = trainerService.registerTrainer(dto);
    //     URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
    //     return ResponseEntity.created(location).body(created);
    // }

    @PutMapping("/{id}")
    // @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Actualizar entrenador", description = "Actualiza los datos de un entrenador")
    public ResponseEntity<TrainerDTO> updateTrainer(@PathVariable Long id, @Valid @RequestBody UpdateTrainerDTO dto) {
        log.info("PUT /api/trainers/{} - Actualizando entrenador", id);
        return ResponseEntity.ok(trainerService.updateTrainer(id, dto));
    }

    @DeleteMapping("/{id}")
    //  @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar entrenador (soft delete)", description = "Marca un entrenador como eliminado")
    public ResponseEntity<Void> deleteTrainer(@PathVariable Long id) {
        log.info("DELETE /api/trainers/{} - Eliminando entrenador", id);
        trainerService.deleteTrainer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    //  @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restaurar entrenador", description = "Restaura un entrenador eliminado")
    public ResponseEntity<Void> restoreTrainer(@PathVariable Long id) {
        log.info("POST /api/trainers/{}/restore - Restaurando entrenador", id);
        trainerService.restoreTrainer(id);
        return ResponseEntity.ok().build();
    }
}
