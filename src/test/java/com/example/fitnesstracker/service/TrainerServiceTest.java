package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.trainer.RegisterTrainerDTO;
import com.example.fitnesstracker.dto.request.trainer.UpdateTrainerDTO;
import com.example.fitnesstracker.dto.response.TrainerDTO;
import com.example.fitnesstracker.enums.UserType;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.ResourceNotFoundException;
import com.example.fitnesstracker.exception.UserAlreadyExistsException;
import com.example.fitnesstracker.mapper.TrainerMapper;
import com.example.fitnesstracker.model.Trainer;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.TrainerRepository;
import com.example.fitnesstracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerService Unit Tests")
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private TrainerService trainerService;

    private RegisterTrainerDTO registerDto;
    private UpdateTrainerDTO updateDto;
    private Trainer trainer;
    private User user;
    private TrainerDTO trainerDto;

    @BeforeEach
    void setUp() {
        registerDto = RegisterTrainerDTO.builder()
                .username("testtrainer")
                .email("trainer@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Coach")
                .specialty("Strength Training")
                .certifications(Arrays.asList("NASM", "ACE"))
                .hourlyRate(BigDecimal.valueOf(50.00))
                .build();

        updateDto = UpdateTrainerDTO.builder()
                .firstName("Jane")
                .lastName("CoachPro")
                .specialty("CrossFit")
                .hourlyRate(BigDecimal.valueOf(75.00))
                .isActive(true)
                .build();

        user = spy(User.builder().id(1L).username("testtrainer").build());
        trainer = spy(Trainer.builder()
                .id(1L)
                .user(user)
                .firstName("John")
                .lastName("Coach")
                .specialty("Strength Training")
                .certifications("NASM,ACE")
                .hourlyRate(BigDecimal.valueOf(50.00))
                .isActive(true)
                .build());

        trainerDto = TrainerDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Coach")
                .fullName("John Coach")
                .specialty("Strength Training")
                .certifications(List.of("NASM", "ACE"))
                .hourlyRate(BigDecimal.valueOf(50.00))
                .isActive(true)
                .assignedMembersCount(0)
                .build();
    }

    // ==================== REGISTER TRAINER TESTS ====================

    @Test
    @DisplayName("registerTrainer - Debería registrar entrenador exitosamente")
    void registerTrainer_Success() {
        when(userService.createUserWithType(anyString(), anyString(), anyString(), eq(UserType.TRAINER))).thenReturn(user);
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);
        when(trainerMapper.toDTO(trainer)).thenReturn(trainerDto);

        TrainerDTO result = trainerService.registerTrainer(registerDto).getTrainer();

        assertThat(result).isEqualTo(trainerDto);
        verify(trainerRepository).save(any(Trainer.class));
        verify(userService).createUserWithType(anyString(), anyString(), anyString(), eq(UserType.TRAINER));
    }

    @Test
    @DisplayName("registerTrainer - Debería lanzar excepción con username duplicado")
    void registerTrainer_DuplicateUsername() {
        doThrow(new UserAlreadyExistsException("nombre de usuario"))
                .when(userService).validateUniqueEmailAndUsername(anyString(), anyString());

        assertThatThrownBy(() -> trainerService.registerTrainer(registerDto))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(trainerRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerTrainer - Debería lanzar excepción con email duplicado")
    void registerTrainer_DuplicateEmail() {
        doThrow(new UserAlreadyExistsException("email"))
                .when(userService).validateUniqueEmailAndUsername(anyString(), anyString());

        assertThatThrownBy(() -> trainerService.registerTrainer(registerDto))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(trainerRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerTrainer - Debería lanzar excepción con tarifa horaria inválida")
    void registerTrainer_InvalidHourlyRate() {
        registerDto.setHourlyRate(BigDecimal.ZERO);

        assertThatThrownBy(() -> trainerService.registerTrainer(registerDto))
                .isInstanceOf(InvalidUserDataException.class);

        verify(trainerRepository, never()).save(any());
    }

    // ==================== GET TRAINERS TESTS ====================

    @Test
    @DisplayName("getTrainerById - Debería retornar entrenador por ID")
    void getTrainerById_Success() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toDTO(trainer)).thenReturn(trainerDto);

        TrainerDTO result = trainerService.getTrainerById(1L);

        assertThat(result).isEqualTo(trainerDto);
    }

    @Test
    @DisplayName("getTrainerById - Debería lanzar excepción con ID inexistente")
    void getTrainerById_NotFound() {
        when(trainerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getTrainerById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Entrenador no encontrado");

        verify(trainerMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("getTrainerByExternalId - Debería retornar entrenador por UUID")
    void getTrainerByExternalId_Success() {
        when(trainerRepository.findByExternalId("ext123")).thenReturn(Optional.of(trainer));
        when(trainerMapper.toDTO(trainer)).thenReturn(trainerDto);

        TrainerDTO result = trainerService.getTrainerByExternalId("ext123");

        assertThat(result).isEqualTo(trainerDto);
    }

    @Test
    @DisplayName("getTrainerByExternalId - Debería lanzar excepción con UUID inexistente")
    void getTrainerByExternalId_NotFound() {
        when(trainerRepository.findByExternalId("invalid-uuid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getTrainerByExternalId("invalid-uuid"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(trainerMapper, never()).toDTO(any());
    }

    @Test
    @SuppressWarnings("deprecation")
    @DisplayName("getAllTrainers - Debería retornar lista de entrenadores activos")
    void getAllTrainers_Success() {
        when(trainerRepository.findAllActiveTrainers()).thenReturn(List.of(trainer));
        when(trainerMapper.toDTO(trainer)).thenReturn(trainerDto);

        List<TrainerDTO> result = trainerService.getAllTrainers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(trainerDto);
    }

    @Test
    @SuppressWarnings("deprecation")
    @DisplayName("getAllTrainers - Debería retornar lista vacía si no hay entrenadores")
    void getAllTrainers_EmptyList() {
        when(trainerRepository.findAllActiveTrainers()).thenReturn(List.of());

        List<TrainerDTO> result = trainerService.getAllTrainers();

        assertThat(result).isEmpty();
        verify(trainerRepository).findAllActiveTrainers();
    }

    @Test
    @SuppressWarnings("deprecation")
    @DisplayName("getAvailableTrainers - Debería retornar lista de entrenadores sin miembros asignados")
    void getAvailableTrainers_Success() {
        when(trainerRepository.findAvailableTrainersWithNoMembers()).thenReturn(List.of(trainer));
        when(trainerMapper.toDTO(trainer)).thenReturn(trainerDto);

        List<TrainerDTO> result = trainerService.getAvailableTrainers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(trainerDto);
    }

    @Test
    @SuppressWarnings("deprecation")
    @DisplayName("getAvailableTrainers - Debería retornar lista vacía si no hay disponibles")
    void getAvailableTrainers_EmptyList() {
        when(trainerRepository.findAvailableTrainersWithNoMembers()).thenReturn(List.of());

        List<TrainerDTO> result = trainerService.getAvailableTrainers();

        assertThat(result).isEmpty();
    }

    // ==================== UPDATE TRAINER TESTS ====================

    @Test
    @DisplayName("updateTrainer - Debería actualizar entrenador exitosamente")
    void updateTrainer_Success() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);
        when(trainerMapper.toDTO(trainer)).thenReturn(trainerDto);

        TrainerDTO result = trainerService.updateTrainer(1L, updateDto);

        assertThat(result).isEqualTo(trainerDto);
        verify(trainerMapper).updateFromDTO(updateDto, trainer);
    }

    @Test
    @DisplayName("updateTrainer - Debería lanzar excepción con ID inexistente")
    void updateTrainer_NotFound() {
        when(trainerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.updateTrainer(999L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(trainerRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateTrainer - Debería lanzar excepción con tarifa horaria inválida")
    void updateTrainer_InvalidHourlyRate() {
        updateDto.setHourlyRate(BigDecimal.ZERO);

        assertThatThrownBy(() -> trainerService.updateTrainer(1L, updateDto))
                .isInstanceOf(InvalidUserDataException.class);

        verify(trainerRepository, never()).save(any());
    }

    // ==================== DELETE TRAINER TESTS ====================

    @Test
    @DisplayName("deleteTrainer - Debería hacer soft delete del entrenador y usuario")
    void deleteTrainer_Success() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.deleteTrainer(1L);

        verify(trainer).softDelete();
        verify(user).softDelete();
        verify(trainerRepository).save(trainer);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("deleteTrainer - Debería lanzar excepción con ID inexistente")
    void deleteTrainer_NotFound() {
        when(trainerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.deleteTrainer(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(trainerRepository, never()).save(any());
    }

    @Test
    @DisplayName("restoreTrainer - Debería restaurar entrenador y usuario eliminado")
    void restoreTrainer_Success() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.restoreTrainer(1L);

        verify(trainer).restore();
        verify(user).restore();
        verify(trainerRepository).save(trainer);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("restoreTrainer - Debería lanzar excepción con ID inexistente")
    void restoreTrainer_NotFound() {
        when(trainerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.restoreTrainer(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(trainerRepository, never()).save(any());
    }

    // ==================== SEARCH TRAINER TESTS ====================

    @Test
    @DisplayName("searchTrainersBySpecialty - Debería retornar lista de entrenadores por especialidad")
    void searchTrainersBySpecialty_Success() {
        when(trainerRepository.findActiveTrainersBySpecialty("Strength")).thenReturn(List.of(trainer));
        when(trainerMapper.toDTO(trainer)).thenReturn(trainerDto);

        List<TrainerDTO> result = trainerService.searchTrainersBySpecialty("Strength");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(trainerDto);
    }

    @Test
    @DisplayName("searchTrainersBySpecialty - Debería retornar lista vacía si no hay coincidencias")
    void searchTrainersBySpecialty_EmptyList() {
        when(trainerRepository.findActiveTrainersBySpecialty("InvalidSpecialty")).thenReturn(List.of());

        List<TrainerDTO> result = trainerService.searchTrainersBySpecialty("InvalidSpecialty");

        assertThat(result).isEmpty();
    }

    @Test
    @SuppressWarnings("deprecation")
    @DisplayName("getMostBusyTrainers - Debería retornar lista de entrenadores más ocupados")
    void getMostBusyTrainers_Success() {
        when(trainerRepository.findMostBusyTrainers()).thenReturn(List.of(trainer));
        when(trainerMapper.toDTO(trainer)).thenReturn(trainerDto);

        List<TrainerDTO> result = trainerService.getMostBusyTrainers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(trainerDto);
    }

    @Test
    @SuppressWarnings("deprecation")
    @DisplayName("getMostBusyTrainers - Debería retornar lista vacía si no hay entrenadores")
    void getMostBusyTrainers_EmptyList() {
        when(trainerRepository.findMostBusyTrainers()).thenReturn(List.of());

        List<TrainerDTO> result = trainerService.getMostBusyTrainers();

        assertThat(result).isEmpty();
    }
}
