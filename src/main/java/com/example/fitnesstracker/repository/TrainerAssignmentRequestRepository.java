package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.enums.RequestStatus;
import com.example.fitnesstracker.model.TrainerAssignmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerAssignmentRequestRepository extends JpaRepository<TrainerAssignmentRequest, Long> {

    // Buscar por member
    List<TrainerAssignmentRequest> findByMember_IdAndDeletedAtIsNull(Long memberId);

    Optional<TrainerAssignmentRequest> findByMember_IdAndStatusAndDeletedAtIsNull(Long memberId, RequestStatus status);

    // Buscar por trainer
    List<TrainerAssignmentRequest> findByTrainer_IdAndDeletedAtIsNull(Long trainerId);

    List<TrainerAssignmentRequest> findByTrainer_IdAndStatusAndDeletedAtIsNull(Long trainerId, RequestStatus status);

    // Verificar si existe solicitud pendiente
    @Query("SELECT COUNT(r) > 0 FROM TrainerAssignmentRequest r WHERE r.member.id = :memberId AND r.status = 'PENDING' AND r.deletedAt IS NULL")
    boolean existsPendingRequestForMember(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(r) > 0 FROM TrainerAssignmentRequest r WHERE r.member.id = :memberId AND r.trainer.id = :trainerId AND r.status = 'PENDING' AND r.deletedAt IS NULL")
    boolean existsPendingRequestBetween(@Param("memberId") Long memberId, @Param("trainerId") Long trainerId);

    // Obtener solicitud activa del member
    @Query("SELECT r FROM TrainerAssignmentRequest r WHERE r.member.id = :memberId AND r.status = 'ACCEPTED' AND r.deletedAt IS NULL")
    Optional<TrainerAssignmentRequest> findActiveAssignmentByMember(@Param("memberId") Long memberId);

    // Contar solicitudes pendientes del trainer
    @Query("SELECT COUNT(r) FROM TrainerAssignmentRequest r WHERE r.trainer.id = :trainerId AND r.status = 'PENDING' AND r.deletedAt IS NULL")
    long countPendingRequestsForTrainer(@Param("trainerId") Long trainerId);
}