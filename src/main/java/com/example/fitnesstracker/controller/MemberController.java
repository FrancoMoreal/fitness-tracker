package com.example.fitnesstracker.controller;

import com.example.fitnesstracker.dto.request.member.RegisterMemberDTO;
import com.example.fitnesstracker.dto.request.member.UpdateMemberDTO;
import com.example.fitnesstracker.dto.response.MemberDTO;
import com.example.fitnesstracker.service.MemberService;
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
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Members", description = "Gestión de miembros del gimnasio")
//@SecurityRequirement(name = "bearerAuth")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    // @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Listar todos los miembros", description = "Obtiene lista de miembros activos")
    public ResponseEntity<List<MemberDTO>> getAllMembers() {
        log.info("GET /api/members - Listando miembros");
        List<MemberDTO> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}")
    // @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Obtener miembro por ID", description = "Busca un miembro por su ID")
    public ResponseEntity<MemberDTO> getMemberById(@PathVariable Long id) {
        log.info("GET /api/members/{} - Obteniendo miembro", id);
        MemberDTO member = memberService.getMemberById(id);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/external/{externalId}")
    //  @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Obtener miembro por External ID", description = "Busca un miembro por su UUID externo")
    public ResponseEntity<MemberDTO> getMemberByExternalId(@PathVariable String externalId) {
        log.info("GET /api/members/external/{} - Obteniendo miembro", externalId);
        MemberDTO member = memberService.getMemberByExternalId(externalId);
        return ResponseEntity.ok(member);
    }

    //   @PostMapping
// @PreAuthorize("hasRole('ADMIN')")
    //*   @Operation(summary = "Crear miembro", description = "Crea un nuevo miembro y su usuario asociado")
    //  public ResponseEntity<MemberDTO> createMember(@Valid @RequestBody RegisterMemberDTO dto) {
    //    log.info("POST /api/members - Creando miembro: {}", dto.getUsername());
    //      MemberDTO created = memberService.registerMember(dto);
    //     URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId())
    //            .toUri();
    //   return ResponseEntity.created(location).body(created);}

    @PutMapping("/{id}")
    // @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Actualizar miembro", description = "Actualiza los datos de un miembro")
    public ResponseEntity<MemberDTO> updateMember(@PathVariable Long id, @Valid @RequestBody UpdateMemberDTO dto) {
        log.info("PUT /api/members/{} - Actualizando miembro", id);
        MemberDTO updatedMember = memberService.updateMember(id, dto);
        return ResponseEntity.ok(updatedMember);
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar miembro (soft delete)", description = "Marca un miembro como eliminado")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        log.info("DELETE /api/members/{} - Eliminando miembro", id);
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    //  @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restaurar miembro", description = "Restaura un miembro eliminado")
    public ResponseEntity<Void> restoreMember(@PathVariable Long id) {
        log.info("POST /api/members/{}/restore - Restaurando miembro", id);
        memberService.restoreMember(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trainer/{trainerId}")
    //  @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Listar miembros de un trainer", description = "Obtiene miembros asignados a un entrenador")
    public ResponseEntity<List<MemberDTO>> getMembersByTrainer(@PathVariable Long trainerId) {
        log.info("GET /api/members/trainer/{} - Obteniendo miembros del trainer", trainerId);
        List<MemberDTO> members = memberService.getMembersByTrainer(trainerId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/expired")
    //  @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar membresías vencidas", description = "Obtiene miembros con membresía vencida")
    public ResponseEntity<List<MemberDTO>> getExpiredMemberships() {
        log.info("GET /api/members/expired - Obteniendo membresías vencidas");
        List<MemberDTO> expiredMembers = memberService.getExpiredMemberships();
        return ResponseEntity.ok(expiredMembers);
    }
}
