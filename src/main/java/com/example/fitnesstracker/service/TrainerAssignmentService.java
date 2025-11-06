package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.assignment.RequestTrainerDTO;
import com.example.fitnesstracker.dto.request.assignment.RespondRequestDTO;
import com.example.fitnesstracker.dto.response.assignment.TrainerAssignmentRequestDTO;
import com.example.fitnesstracker.enums.AssignmentStatus;
import com.example.fitnesstracker.enums.RequestStatus;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.ResourceNotFoundException;
import com.example.fitnesstracker.mapper.TrainerAssignmentRequestMapper;
import com.example.fitnesstracker.model.Member;
import com.example.fitnesstracker.model.Trainer;
import com.example.fitnesstracker.model.TrainerAssignmentRequest;
import com.example.fitnesstracker.repository.MemberRepository;
import com.example.fitnesstracker.repository.TrainerAssignmentRequestRepository;
import com.example.fitnesstracker.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerAssignmentService {

    private final TrainerAssignmentRequestRepository requestRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final TrainerAssignmentRequestMapper requestMapper;

    @Transactional
    public TrainerAssignmentRequestDTO requestTrainer(Long memberId, RequestTrainerDTO dto) {
        log.info("Member {} solicitando trainer {}", memberId, dto.getTrainerId());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Miembro no encontrado"));

        if (member.isDeleted()) {
            throw new ResourceNotFoundException("Miembro no encontrado");
        }

        if (requestRepository.existsPendingRequestForMember(memberId)) {
            throw new InvalidUserDataException("Ya tienes una solicitud pendiente");
        }

        if (member.getAssignedTrainer() != null && member.getAssignmentStatus() == AssignmentStatus.ACTIVE) {
            throw new InvalidUserDataException("Ya tienes un entrenador asignado");
        }

        Trainer trainer = trainerRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Entrenador no encontrado"));

        if (trainer.isDeleted() || !trainer.getIsActive()) {
            throw new ResourceNotFoundException("Entrenador no disponible");
        }

        TrainerAssignmentRequest request = TrainerAssignmentRequest.builder()
                .member(member)
                .trainer(trainer)
                .status(RequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .memberMessage(dto.getMessage())
                .build();

        TrainerAssignmentRequest savedRequest = requestRepository.save(request);

        member.setAssignmentStatus(AssignmentStatus.PENDING);
        memberRepository.save(member);

        log.info("Solicitud creada: Member {} -> Trainer {}", memberId, dto.getTrainerId());

        return requestMapper.toDTO(savedRequest);
    }

    @Transactional
    public TrainerAssignmentRequestDTO acceptRequest(Long requestId, Long trainerId, RespondRequestDTO dto) {
        log.info("Trainer {} aceptando solicitud {}", trainerId, requestId);

        TrainerAssignmentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (request.isDeleted()) {
            throw new ResourceNotFoundException("Solicitud no encontrada");
        }

        if (!request.getTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("No tienes permiso para responder esta solicitud");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidUserDataException("Esta solicitud ya fue respondida");
        }

        request.setStatus(RequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());
        request.setTrainerResponse(dto.getResponse());

        TrainerAssignmentRequest updatedRequest = requestRepository.save(request);

        Member member = request.getMember();
        member.setAssignedTrainer(request.getTrainer());
        member.setAssignmentStatus(AssignmentStatus.ACTIVE);
        memberRepository.save(member);

        log.info("Solicitud aceptada: Member {} ahora tiene Trainer {}", member.getId(), trainerId);

        return requestMapper.toDTO(updatedRequest);
    }

    @Transactional
    public TrainerAssignmentRequestDTO rejectRequest(Long requestId, Long trainerId, RespondRequestDTO dto) {
        log.info("Trainer {} rechazando solicitud {}", trainerId, requestId);

        TrainerAssignmentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (request.isDeleted()) {
            throw new ResourceNotFoundException("Solicitud no encontrada");
        }

        if (!request.getTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("No tienes permiso para responder esta solicitud");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidUserDataException("Esta solicitud ya fue respondida");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRespondedAt(LocalDateTime.now());
        request.setTrainerResponse(dto.getResponse());

        TrainerAssignmentRequest updatedRequest = requestRepository.save(request);

        Member member = request.getMember();
        member.setAssignmentStatus(AssignmentStatus.REJECTED);
        memberRepository.save(member);

        log.info("Solicitud rechazada: Member {}", member.getId());

        return requestMapper.toDTO(updatedRequest);
    }

    @Transactional
    public void cancelRequest(Long requestId, Long memberId) {
        log.info("Member {} cancelando solicitud {}", memberId, requestId);

        TrainerAssignmentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (request.isDeleted()) {
            throw new ResourceNotFoundException("Solicitud no encontrada");
        }

        if (!request.getMember().getId().equals(memberId)) {
            throw new InvalidUserDataException("No tienes permiso para cancelar esta solicitud");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidUserDataException("Solo puedes cancelar solicitudes pendientes");
        }

        request.setStatus(RequestStatus.CANCELLED);
        request.setRespondedAt(LocalDateTime.now());
        requestRepository.save(request);

        Member member = request.getMember();
        member.setAssignmentStatus(AssignmentStatus.NO_TRAINER);
        memberRepository.save(member);

        log.info("Solicitud cancelada: {}", requestId);
    }

    @Transactional
    public void removeTrainer(Long memberId) {
        log.info("Member {} removiendo su trainer", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Miembro no encontrado"));

        if (member.isDeleted()) {
            throw new ResourceNotFoundException("Miembro no encontrado");
        }

        if (member.getAssignedTrainer() == null) {
            throw new InvalidUserDataException("No tienes un entrenador asignado");
        }

        member.setAssignedTrainer(null);
        member.setAssignmentStatus(AssignmentStatus.NO_TRAINER);
        memberRepository.save(member);

        log.info("Trainer removido del member {}", memberId);
    }

    public List<TrainerAssignmentRequestDTO> getPendingRequestsForTrainer(Long trainerId) {
        log.debug("Obteniendo solicitudes pendientes del trainer {}", trainerId);

        return requestRepository.findByTrainer_IdAndStatusAndDeletedAtIsNull(trainerId, RequestStatus.PENDING)
                .stream()
                .map(requestMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<TrainerAssignmentRequestDTO> getAllRequestsForTrainer(Long trainerId) {
        log.debug("Obteniendo todas las solicitudes del trainer {}", trainerId);

        return requestRepository.findByTrainer_IdAndDeletedAtIsNull(trainerId)
                .stream()
                .map(requestMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<TrainerAssignmentRequestDTO> getRequestsForMember(Long memberId) {
        log.debug("Obteniendo solicitudes del member {}", memberId);

        return requestRepository.findByMember_IdAndDeletedAtIsNull(memberId)
                .stream()
                .map(requestMapper::toDTO)
                .collect(Collectors.toList());
    }

    public TrainerAssignmentRequestDTO getRequestById(Long requestId) {
        log.debug("Obteniendo solicitud {}", requestId);

        TrainerAssignmentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (request.isDeleted()) {
            throw new ResourceNotFoundException("Solicitud no encontrada");
        }

        return requestMapper.toDTO(request);
    }

    public long countPendingRequestsForTrainer(Long trainerId) {
        return requestRepository.countPendingRequestsForTrainer(trainerId);
    }
}