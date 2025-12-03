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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerAssignmentService Unit Tests")
class TrainerAssignmentServiceTest {

    @Mock
    private TrainerAssignmentRequestRepository requestRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainerAssignmentRequestMapper requestMapper;

    @InjectMocks
    private TrainerAssignmentService assignmentService;

    private RequestTrainerDTO requestDto;
    private RespondRequestDTO respondDto;
    private Member member;
    private Trainer trainer;
    private TrainerAssignmentRequest request;
    private TrainerAssignmentRequestDTO requestDtoResponse;

    @BeforeEach
    void setUp() {
        requestDto = RequestTrainerDTO.builder()
                .trainerId(1L)
                .message("Quiero trabajar contigo")
                .build();

        respondDto = RespondRequestDTO.builder()
                .response("Excelente, vamos a trabajar juntos")
                .build();

        member = spy(Member.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .assignmentStatus(AssignmentStatus.NO_TRAINER)
                .build());

        trainer = spy(Trainer.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Coach")
                .isActive(true)
                .build());

        request = spy(TrainerAssignmentRequest.builder()
                .id(1L)
                .member(member)
                .trainer(trainer)
                .status(RequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .memberMessage("Quiero trabajar contigo")
                .build());

        requestDtoResponse = TrainerAssignmentRequestDTO.builder()
                .id(1L)
                .memberId(1L)
                .memberName("John Doe")
                .trainerId(1L)
                .trainerName("Jane Coach")
                .status(RequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .memberMessage("Quiero trabajar contigo")
                .build();
    }

    // ==================== REQUEST TRAINER TESTS ====================

    @Test
    @DisplayName("requestTrainer - Debería crear solicitud exitosamente")
    void requestTrainer_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(requestRepository.existsPendingRequestForMember(1L)).thenReturn(false);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(requestRepository.save(any(TrainerAssignmentRequest.class))).thenReturn(request);
        when(requestMapper.toDTO(request)).thenReturn(requestDtoResponse);

        TrainerAssignmentRequestDTO result = assignmentService.requestTrainer(1L, requestDto);

        assertThat(result).isEqualTo(requestDtoResponse);
        verify(requestRepository).save(any(TrainerAssignmentRequest.class));
        verify(memberRepository).save(member);
        verify(member).setAssignmentStatus(AssignmentStatus.PENDING);
    }

    @Test
    @DisplayName("requestTrainer - Debería lanzar excepción con miembro no encontrado")
    void requestTrainer_MemberNotFound() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.requestTrainer(999L, requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Miembro no encontrado");

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("requestTrainer - Debería lanzar excepción con miembro eliminado")
    void requestTrainer_MemberDeleted() {
        member.softDelete();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> assignmentService.requestTrainer(1L, requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Miembro no encontrado");

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("requestTrainer - Debería lanzar excepción con solicitud pendiente existente")
    void requestTrainer_PendingRequestExists() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(requestRepository.existsPendingRequestForMember(1L)).thenReturn(true);

        assertThatThrownBy(() -> assignmentService.requestTrainer(1L, requestDto))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Ya tienes una solicitud pendiente");

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("requestTrainer - Debería lanzar excepción si ya tiene entrenador asignado")
    void requestTrainer_TrainerAlreadyAssigned() {
        member.setAssignedTrainer(trainer);
        member.setAssignmentStatus(AssignmentStatus.ACTIVE);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(requestRepository.existsPendingRequestForMember(1L)).thenReturn(false);

        assertThatThrownBy(() -> assignmentService.requestTrainer(1L, requestDto))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Ya tienes un entrenador asignado");

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("requestTrainer - Debería lanzar excepción con entrenador no encontrado")
    void requestTrainer_TrainerNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(requestRepository.existsPendingRequestForMember(1L)).thenReturn(false);
        when(trainerRepository.findById(999L)).thenReturn(Optional.empty());

        requestDto.setTrainerId(999L);

        assertThatThrownBy(() -> assignmentService.requestTrainer(1L, requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Entrenador no encontrado");

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("requestTrainer - Debería lanzar excepción con entrenador inactivo")
    void requestTrainer_TrainerInactive() {
        trainer.setIsActive(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(requestRepository.existsPendingRequestForMember(1L)).thenReturn(false);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> assignmentService.requestTrainer(1L, requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Entrenador no disponible");

        verify(requestRepository, never()).save(any());
    }

    // ==================== ACCEPT REQUEST TESTS ====================

    @Test
    @DisplayName("acceptRequest - Debería aceptar solicitud exitosamente")
    void acceptRequest_Success() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);
        when(memberRepository.save(member)).thenReturn(member);
        when(requestMapper.toDTO(request)).thenReturn(requestDtoResponse);

        TrainerAssignmentRequestDTO result = assignmentService.acceptRequest(1L, 1L, respondDto);

        assertThat(result).isEqualTo(requestDtoResponse);
        verify(request).setStatus(RequestStatus.ACCEPTED);
        verify(member).setAssignedTrainer(trainer);
        verify(member).setAssignmentStatus(AssignmentStatus.ACTIVE);
    }

    @Test
    @DisplayName("acceptRequest - Debería lanzar excepción con solicitud no encontrada")
    void acceptRequest_RequestNotFound() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.acceptRequest(999L, 1L, respondDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Solicitud no encontrada");

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("acceptRequest - Debería lanzar excepción si el trainer no tiene permiso")
    void acceptRequest_UnauthorizedTrainer() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> assignmentService.acceptRequest(1L, 999L, respondDto))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("No tienes permiso para responder esta solicitud");

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("acceptRequest - Debería lanzar excepción si solicitud ya fue respondida")
    void acceptRequest_AlreadyResponded() {
        request.setStatus(RequestStatus.ACCEPTED);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> assignmentService.acceptRequest(1L, 1L, respondDto))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Esta solicitud ya fue respondida");

        verify(requestRepository, never()).save(any());
    }

    // ==================== REJECT REQUEST TESTS ====================

    @Test
    @DisplayName("rejectRequest - Debería rechazar solicitud exitosamente")
    void rejectRequest_Success() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);
        when(memberRepository.save(member)).thenReturn(member);
        when(requestMapper.toDTO(request)).thenReturn(requestDtoResponse);

        TrainerAssignmentRequestDTO result = assignmentService.rejectRequest(1L, 1L, respondDto);

        assertThat(result).isEqualTo(requestDtoResponse);
        verify(request).setStatus(RequestStatus.REJECTED);
        verify(member).setAssignmentStatus(AssignmentStatus.REJECTED);
    }

    @Test
    @DisplayName("rejectRequest - Debería lanzar excepción con solicitud no encontrada")
    void rejectRequest_RequestNotFound() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.rejectRequest(999L, 1L, respondDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Solicitud no encontrada");

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("rejectRequest - Debería lanzar excepción si el trainer no tiene permiso")
    void rejectRequest_UnauthorizedTrainer() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> assignmentService.rejectRequest(1L, 999L, respondDto))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("No tienes permiso para responder esta solicitud");

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("rejectRequest - Debería lanzar excepción si solicitud ya fue respondida")
    void rejectRequest_AlreadyResponded() {
        request.setStatus(RequestStatus.REJECTED);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> assignmentService.rejectRequest(1L, 1L, respondDto))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Esta solicitud ya fue respondida");

        verify(requestRepository, never()).save(any());
    }

    // ==================== CANCEL REQUEST TESTS ====================

    @Test
    @DisplayName("cancelRequest - Debería cancelar solicitud exitosamente")
    void cancelRequest_Success() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);
        when(memberRepository.save(member)).thenReturn(member);

        assignmentService.cancelRequest(1L, 1L);

        verify(request).setStatus(RequestStatus.CANCELLED);
        verify(member).setAssignmentStatus(AssignmentStatus.NO_TRAINER);
        verify(requestRepository).save(request);
    }

    @Test
    @DisplayName("cancelRequest - Debería lanzar excepción con solicitud no encontrada")
    void cancelRequest_RequestNotFound() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.cancelRequest(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Solicitud no encontrada");

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelRequest - Debería lanzar excepción si el miembro no tiene permiso")
    void cancelRequest_UnauthorizedMember() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> assignmentService.cancelRequest(1L, 999L))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("No tienes permiso para cancelar esta solicitud");

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelRequest - Debería lanzar excepción si solicitud no está pendiente")
    void cancelRequest_NotPending() {
        request.setStatus(RequestStatus.ACCEPTED);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> assignmentService.cancelRequest(1L, 1L))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Solo puedes cancelar solicitudes pendientes");

        verify(requestRepository, never()).save(any());
    }

    // ==================== REMOVE TRAINER TESTS ====================

    @Test
    @DisplayName("removeTrainer - Debería remover entrenador exitosamente")
    void removeTrainer_Success() {
        member.setAssignedTrainer(trainer);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);

        assignmentService.removeTrainer(1L);

        verify(member).setAssignedTrainer(null);
        verify(member).setAssignmentStatus(AssignmentStatus.NO_TRAINER);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("removeTrainer - Debería lanzar excepción con miembro no encontrado")
    void removeTrainer_MemberNotFound() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.removeTrainer(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Miembro no encontrado");

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeTrainer - Debería lanzar excepción si miembro no tiene entrenador")
    void removeTrainer_NoTrainerAssigned() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> assignmentService.removeTrainer(1L))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("No tienes un entrenador asignado");

        verify(memberRepository, never()).save(any());
    }

    // ==================== GET REQUESTS TESTS ====================

    @Test
    @DisplayName("getPendingRequestsForTrainer - Debería retornar solicitudes pendientes del trainer")
    void getPendingRequestsForTrainer_Success() {
        when(requestRepository.findByTrainer_IdAndStatusAndDeletedAtIsNull(1L, RequestStatus.PENDING))
                .thenReturn(Arrays.asList(request));
        when(requestMapper.toDTO(request)).thenReturn(requestDtoResponse);

        List<TrainerAssignmentRequestDTO> result = assignmentService.getPendingRequestsForTrainer(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(requestDtoResponse);
    }

    @Test
    @DisplayName("getPendingRequestsForTrainer - Debería retornar lista vacía si no hay solicitudes")
    void getPendingRequestsForTrainer_EmptyList() {
        when(requestRepository.findByTrainer_IdAndStatusAndDeletedAtIsNull(1L, RequestStatus.PENDING))
                .thenReturn(List.of());

        List<TrainerAssignmentRequestDTO> result = assignmentService.getPendingRequestsForTrainer(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAllRequestsForTrainer - Debería retornar todas las solicitudes del trainer")
    void getAllRequestsForTrainer_Success() {
        when(requestRepository.findByTrainer_IdAndDeletedAtIsNull(1L))
                .thenReturn(Arrays.asList(request));
        when(requestMapper.toDTO(request)).thenReturn(requestDtoResponse);

        List<TrainerAssignmentRequestDTO> result = assignmentService.getAllRequestsForTrainer(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(requestDtoResponse);
    }

    @Test
    @DisplayName("getAllRequestsForTrainer - Debería retornar lista vacía")
    void getAllRequestsForTrainer_EmptyList() {
        when(requestRepository.findByTrainer_IdAndDeletedAtIsNull(1L))
                .thenReturn(List.of());

        List<TrainerAssignmentRequestDTO> result = assignmentService.getAllRequestsForTrainer(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getRequestsForMember - Debería retornar solicitudes del miembro")
    void getRequestsForMember_Success() {
        when(requestRepository.findByMember_IdAndDeletedAtIsNull(1L))
                .thenReturn(Arrays.asList(request));
        when(requestMapper.toDTO(request)).thenReturn(requestDtoResponse);

        List<TrainerAssignmentRequestDTO> result = assignmentService.getRequestsForMember(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(requestDtoResponse);
    }

    @Test
    @DisplayName("getRequestsForMember - Debería retornar lista vacía")
    void getRequestsForMember_EmptyList() {
        when(requestRepository.findByMember_IdAndDeletedAtIsNull(1L))
                .thenReturn(List.of());

        List<TrainerAssignmentRequestDTO> result = assignmentService.getRequestsForMember(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getRequestById - Debería retornar solicitud por ID")
    void getRequestById_Success() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestMapper.toDTO(request)).thenReturn(requestDtoResponse);

        TrainerAssignmentRequestDTO result = assignmentService.getRequestById(1L);

        assertThat(result).isEqualTo(requestDtoResponse);
    }

    @Test
    @DisplayName("getRequestById - Debería lanzar excepción con solicitud no encontrada")
    void getRequestById_NotFound() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.getRequestById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Solicitud no encontrada");
    }

    @Test
    @DisplayName("countPendingRequestsForTrainer - Debería contar solicitudes pendientes")
    void countPendingRequestsForTrainer_Success() {
        when(requestRepository.countPendingRequestsForTrainer(1L)).thenReturn(3L);

        long result = assignmentService.countPendingRequestsForTrainer(1L);

        assertThat(result).isEqualTo(3L);
        verify(requestRepository).countPendingRequestsForTrainer(1L);
    }

    @Test
    @DisplayName("countPendingRequestsForTrainer - Debería retornar cero si no hay solicitudes")
    void countPendingRequestsForTrainer_Zero() {
        when(requestRepository.countPendingRequestsForTrainer(1L)).thenReturn(0L);

        long result = assignmentService.countPendingRequestsForTrainer(1L);

        assertThat(result).isZero();
    }
}
