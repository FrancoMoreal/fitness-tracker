package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.workout.*;
import com.example.fitnesstracker.dto.response.workout.*;
import com.example.fitnesstracker.enums.AssignmentStatus;
import com.example.fitnesstracker.enums.WorkoutPlanStatus;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.ResourceNotFoundException;
import com.example.fitnesstracker.mapper.*;
import com.example.fitnesstracker.model.*;
import com.example.fitnesstracker.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkoutPlanService Unit Tests")
class WorkoutPlanServiceTest {

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;
    @Mock
    private WorkoutDayRepository workoutDayRepository;
    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;
    @Mock
    private WorkoutCompletionRepository workoutCompletionRepository;
    @Mock
    private ExerciseLogRepository exerciseLogRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private WorkoutPlanMapper workoutPlanMapper;
    @Mock
    private WorkoutDayMapper workoutDayMapper;
    @Mock
    private WorkoutExerciseMapper workoutExerciseMapper;
    @Mock
    private WorkoutCompletionMapper workoutCompletionMapper;

    @InjectMocks
    private WorkoutPlanService workoutPlanService;

    private Trainer testTrainer;
    private Member testMember;
    private WorkoutPlan testPlan;
    private WorkoutDay testDay;
    private WorkoutExercise testExercise;
    private Exercise testExerciseEntity;
    private WorkoutCompletion testCompletion;
    private WorkoutPlanDTO testPlanDTO;
    private WorkoutDayDTO testDayDTO;
    private WorkoutExerciseDTO testExerciseDTO;
    private WorkoutCompletionDTO testCompletionDTO;

    @BeforeEach
    void setUp() {
        // Trainer
        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setFirstName("John");
        testTrainer.setLastName("Trainer");
        testTrainer.setIsActive(true);

        // Member
        testMember = new Member();
        testMember.setId(1L);
        testMember.setFirstName("Jane");
        testMember.setLastName("Member");
        testMember.setAssignedTrainer(testTrainer);
        testMember.setAssignmentStatus(AssignmentStatus.ACTIVE);

        // WorkoutPlan
        testPlan = WorkoutPlan.builder()
                .name("Test Plan")
                .description("Test Description")
                .member(testMember)
                .trainer(testTrainer)
                .status(WorkoutPlanStatus.DRAFT)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .workoutDays(new ArrayList<>())
                .build();

        // WorkoutDay
        testDay = WorkoutDay.builder()
                .workoutPlan(testPlan)
                .dayName("Day 1")
                .dayNumber(1)
                .exercises(new ArrayList<>())
                .build();

        // Exercise
        testExerciseEntity = new Exercise();
        testExerciseEntity.setId(1L);
        testExerciseEntity.setName("Squat");

        // WorkoutExercise
        testExercise = WorkoutExercise.builder()
                .workoutDay(testDay)
                .exercise(testExerciseEntity)
                .sets(3)
                .reps(10)
                .weight(100.0)
                .restSeconds(60)
                .orderInWorkout(1)
                .build();

        // WorkoutCompletion
        testCompletion = WorkoutCompletion.builder()
                .member(testMember)
                .workoutDay(testDay)
                .completedAt(LocalDate.now())
                .rating(5)
                .exerciseLogs(new ArrayList<>())
                .build();

        // DTOs
        testPlanDTO = new WorkoutPlanDTO();
        testPlanDTO.setId(1L);
        testPlanDTO.setName("Test Plan");

        testDayDTO = new WorkoutDayDTO();
        testDayDTO.setId(1L);
        testDayDTO.setDayName("Day 1");

        testExerciseDTO = new WorkoutExerciseDTO();
        testExerciseDTO.setId(1L);
        testExerciseDTO.setExerciseName("Squat");

        testCompletionDTO = new WorkoutCompletionDTO();
        testCompletionDTO.setId(1L);
    }

    // ==================== CREATE WORKOUT PLAN TESTS ====================

    @Test
    @DisplayName("createWorkoutPlan - Debería crear plan exitosamente")
    void createWorkoutPlan_Success() {

        CreateWorkoutPlanDTO dto = CreateWorkoutPlanDTO.builder()
                .name("New Plan")
                .description("Description")
                .memberId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();

        when(trainerRepository.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(testPlan);
        when(workoutPlanMapper.toDTO(any(WorkoutPlan.class), anyBoolean())).thenReturn(testPlanDTO);

        WorkoutPlanDTO result = workoutPlanService.createWorkoutPlan(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(trainerRepository).findById(1L);
        verify(memberRepository).findById(1L);
        verify(workoutPlanRepository).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("createWorkoutPlan - Debería lanzar excepción si trainer no encontrado")
    void createWorkoutPlan_TrainerNotFound() {

        CreateWorkoutPlanDTO dto = CreateWorkoutPlanDTO.builder()
                .memberId(1L)
                .name("Plan")
                .startDate(LocalDate.now())
                .build();

        when(trainerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutPlanService.createWorkoutPlan(999L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Entrenador no encontrado");

        verify(memberRepository, never()).findById(any());
    }

    @Test
    @DisplayName("createWorkoutPlan - Debería lanzar excepción si trainer está inactivo")
    void createWorkoutPlan_TrainerInactive() {
        testTrainer.setIsActive(false);
        CreateWorkoutPlanDTO dto = CreateWorkoutPlanDTO.builder()
                .memberId(1L)
                .name("Plan")
                .startDate(LocalDate.now())
                .build();

        when(trainerRepository.findById(1L)).thenReturn(Optional.of(testTrainer));

        assertThatThrownBy(() -> workoutPlanService.createWorkoutPlan(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Entrenador no disponible");
    }

    @Test
    @DisplayName("createWorkoutPlan - Debería lanzar excepción si member no encontrado")
    void createWorkoutPlan_MemberNotFound() {

        CreateWorkoutPlanDTO dto = CreateWorkoutPlanDTO.builder()
                .memberId(999L)
                .name("Plan")
                .startDate(LocalDate.now())
                .build();

        when(trainerRepository.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutPlanService.createWorkoutPlan(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Miembro no encontrado");
    }

    @Test
    @DisplayName("createWorkoutPlan - Debería lanzar excepción si member no asignado al trainer")
    void createWorkoutPlan_MemberNotAssignedToTrainer() {

        Trainer otherTrainer = new Trainer();
        otherTrainer.setId(2L);
        testMember.setAssignedTrainer(otherTrainer);

        CreateWorkoutPlanDTO dto = CreateWorkoutPlanDTO.builder()
                .memberId(1L)
                .name("Plan")
                .startDate(LocalDate.now())
                .build();

        when(trainerRepository.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));


        assertThatThrownBy(() -> workoutPlanService.createWorkoutPlan(1L, dto))
                .isInstanceOf(InvalidUserDataException.class);
    }

    // ==================== ADD WORKOUT DAY TESTS ====================

    @Test
    @DisplayName("addWorkoutDay - Debería agregar día exitosamente")
    void addWorkoutDay_Success() {
        // Arrange
        AddWorkoutDayDTO dto = AddWorkoutDayDTO.builder()
                .dayName("Día 1")
                .dayNumber(1)
                .notes("Test notes")
                .build();

        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(workoutDayRepository.save(any(WorkoutDay.class))).thenReturn(testDay);
        when(workoutDayMapper.toDTO(any(WorkoutDay.class), anyBoolean())).thenReturn(testDayDTO);

        WorkoutDayDTO result = workoutPlanService.addWorkoutDay(1L, 1L, dto);

        assertThat(result).isNotNull();
        verify(workoutPlanRepository).findById(1L);
        verify(workoutDayRepository).save(any(WorkoutDay.class));
    }

    @Test
    @DisplayName("addWorkoutDay - Debería lanzar excepción si plan no encontrado")
    void addWorkoutDay_PlanNotFound() {

        AddWorkoutDayDTO dto = new AddWorkoutDayDTO();
        when(workoutPlanRepository.findById(999L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> workoutPlanService.addWorkoutDay(999L, 1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Plan de workout no encontrado");
    }

    @Test
    @DisplayName("addWorkoutDay - Debería lanzar excepción si trainer no es el creador")
    void addWorkoutDay_TrainerNotOwner() {

        AddWorkoutDayDTO dto = new AddWorkoutDayDTO();
        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        assertThatThrownBy(() -> workoutPlanService.addWorkoutDay(1L, 999L, dto))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessage("No tienes permiso para modificar este plan");
    }

    // ==================== ADD EXERCISE TO DAY TESTS ====================

    @Test
    @DisplayName("addExerciseToDay - Debería agregar ejercicio exitosamente")
    void addExerciseToDay_Success() {

        AddExerciseToWorkoutDTO dto = AddExerciseToWorkoutDTO.builder()
                .exerciseId(1L)
                .sets(3)
                .reps(10)
                .weight(100.0)
                .restSeconds(60)
                .orderInWorkout(1)
                .build();

        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(testDay));
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(testExerciseEntity));
        when(workoutExerciseRepository.save(any(WorkoutExercise.class))).thenReturn(testExercise);
        when(workoutExerciseMapper.toDTO(any(WorkoutExercise.class))).thenReturn(testExerciseDTO);


        WorkoutExerciseDTO result = workoutPlanService.addExerciseToDay(1L, 1L, dto);

        assertThat(result).isNotNull();
        verify(workoutDayRepository).findById(1L);
        verify(exerciseRepository).findById(1L);
        verify(workoutExerciseRepository).save(any(WorkoutExercise.class));
    }

    @Test
    @DisplayName("addExerciseToDay - Debería lanzar excepción si día no encontrado")
    void addExerciseToDay_DayNotFound() {

        AddExerciseToWorkoutDTO dto = new AddExerciseToWorkoutDTO();
        when(workoutDayRepository.findById(999L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> workoutPlanService.addExerciseToDay(999L, 1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Día de workout no encontrado");
    }

    @Test
    @DisplayName("addExerciseToDay - Debería lanzar excepción si ejercicio no encontrado")
    void addExerciseToDay_ExerciseNotFound() {

        AddExerciseToWorkoutDTO dto = AddExerciseToWorkoutDTO.builder()
                .exerciseId(999L)
                .sets(3)
                .reps(10)
                .orderInWorkout(1)
                .build();

        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(testDay));
        when(exerciseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutPlanService.addExerciseToDay(1L, 1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ejercicio no encontrado");
    }

    // ==================== ACTIVATE WORKOUT PLAN TESTS ====================

    @Test
    @DisplayName("activateWorkoutPlan - Debería activar plan exitosamente")
    void activateWorkoutPlan_Success() {

        testPlan.getWorkoutDays().add(testDay);
        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(testPlan);
        when(workoutPlanMapper.toDTO(any(WorkoutPlan.class), anyBoolean())).thenReturn(testPlanDTO);


        WorkoutPlanDTO result = workoutPlanService.activateWorkoutPlan(1L, 1L);

        assertThat(result).isNotNull();
        verify(workoutPlanRepository).save(any(WorkoutPlan.class));
        assertThat(testPlan.getStatus()).isEqualTo(WorkoutPlanStatus.ACTIVE);
    }

    @Test
    @DisplayName("activateWorkoutPlan - Debería lanzar excepción si plan no tiene días")
    void activateWorkoutPlan_NoDays() {

        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));


        assertThatThrownBy(() -> workoutPlanService.activateWorkoutPlan(1L, 1L))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("al menos un día");
    }

    // ==================== GET WORKOUT PLAN TESTS ====================

    @Test
    @DisplayName("getWorkoutPlanById - Debería retornar plan")
    void getWorkoutPlanById_Success() {

        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(workoutPlanMapper.toDTO(any(WorkoutPlan.class), anyBoolean())).thenReturn(testPlanDTO);


        WorkoutPlanDTO result = workoutPlanService.getWorkoutPlanById(1L);


        assertThat(result).isNotNull();
        verify(workoutPlanRepository).findById(1L);
    }

    @Test
    @DisplayName("getWorkoutPlanById - Debería lanzar excepción si no encontrado")
    void getWorkoutPlanById_NotFound() {

        when(workoutPlanRepository.findById(999L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> workoutPlanService.getWorkoutPlanById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Plan de workout no encontrado");
    }

    @Test
    @DisplayName("getWorkoutPlansByMember - Debería retornar lista de planes")
    void getWorkoutPlansByMember_Success() {

        when(workoutPlanRepository.findByMember_IdAndDeletedAtIsNull(1L))
                .thenReturn(Arrays.asList(testPlan));
        when(workoutPlanMapper.toDTO(any(WorkoutPlan.class), anyBoolean()))
                .thenReturn(testPlanDTO);


        List<WorkoutPlanDTO> result = workoutPlanService.getWorkoutPlansByMember(1L);


        assertThat(result).hasSize(1);
        verify(workoutPlanRepository).findByMember_IdAndDeletedAtIsNull(1L);
    }

    @Test
    @DisplayName("getActiveWorkoutPlansByMember - Debería retornar planes activos")
    void getActiveWorkoutPlansByMember_Success() {

        testPlan.setStatus(WorkoutPlanStatus.ACTIVE);
        when(workoutPlanRepository.findActivePlansByMember(1L))
                .thenReturn(Arrays.asList(testPlan));
        when(workoutPlanMapper.toDTO(any(WorkoutPlan.class), anyBoolean()))
                .thenReturn(testPlanDTO);


        List<WorkoutPlanDTO> result = workoutPlanService.getActiveWorkoutPlansByMember(1L);

        assertThat(result).hasSize(1);
        verify(workoutPlanRepository).findActivePlansByMember(1L);
    }

    // ==================== DELETE WORKOUT PLAN TESTS ====================

    @Test
    @DisplayName("deleteWorkoutPlan - Debería eliminar plan exitosamente")
    void deleteWorkoutPlan_Success() {

        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(testPlan);


        workoutPlanService.deleteWorkoutPlan(1L, 1L);


        verify(workoutPlanRepository).findById(1L);
        verify(workoutPlanRepository).save(testPlan);
        assertThat(testPlan.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteWorkoutPlan - Debería lanzar excepción si trainer no es el creador")
    void deleteWorkoutPlan_TrainerNotOwner() {

        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        assertThatThrownBy(() -> workoutPlanService.deleteWorkoutPlan(1L, 999L))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessage("No tienes permiso para eliminar este plan");
    }

    // ==================== COMPLETE WORKOUT TESTS ====================

    @Test
    @DisplayName("completeWorkout - Debería completar workout exitosamente")
    void completeWorkout_Success() {

        CompleteWorkoutDTO dto = CompleteWorkoutDTO.builder()
                .rating(5)
                .notes("Great workout")
                .exerciseLogs(new ArrayList<>())
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(testDay));
        when(workoutCompletionRepository.save(any(WorkoutCompletion.class))).thenReturn(testCompletion);
        when(workoutCompletionMapper.toDTO(any(WorkoutCompletion.class))).thenReturn(testCompletionDTO);

        WorkoutCompletionDTO result = workoutPlanService.completeWorkout(1L, 1L, dto);

        assertThat(result).isNotNull();
        verify(workoutCompletionRepository).save(any(WorkoutCompletion.class));
    }

    @Test
    @DisplayName("completeWorkout - Debería lanzar excepción si member no encontrado")
    void completeWorkout_MemberNotFound() {

        CompleteWorkoutDTO dto = new CompleteWorkoutDTO();
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutPlanService.completeWorkout(999L, 1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Miembro no encontrado");
    }

    @Test
    @DisplayName("completeWorkout - Debería lanzar excepción si workout no es del member")
    void completeWorkout_WorkoutNotOwnedByMember() {

        Member otherMember = new Member();
        otherMember.setId(2L);
        testDay.getWorkoutPlan().setMember(otherMember);

        CompleteWorkoutDTO dto = new CompleteWorkoutDTO();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(testDay));

        assertThatThrownBy(() -> workoutPlanService.completeWorkout(1L, 1L, dto))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessage("Este workout no es tuyo");
    }

    @Test
    @DisplayName("completeWorkout - Debería guardar exercise logs")
    void completeWorkout_WithExerciseLogs() {

        CompleteWorkoutDTO.ExerciseLogDTO logDTO = CompleteWorkoutDTO.ExerciseLogDTO.builder()
                .workoutExerciseId(1L)
                .setsCompleted(3)
                .repsCompleted(10)
                .weightUsed(100.0)
                .build();

        CompleteWorkoutDTO dto = CompleteWorkoutDTO.builder()
                .rating(5)
                .exerciseLogs(Arrays.asList(logDTO))
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(testDay));
        when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(testExercise));
        when(workoutCompletionRepository.save(any(WorkoutCompletion.class))).thenReturn(testCompletion);
        when(exerciseLogRepository.save(any(ExerciseLog.class))).thenReturn(new ExerciseLog());
        when(workoutCompletionMapper.toDTO(any(WorkoutCompletion.class))).thenReturn(testCompletionDTO);

        WorkoutCompletionDTO result = workoutPlanService.completeWorkout(1L, 1L, dto);

        assertThat(result).isNotNull();
        verify(exerciseLogRepository).save(any(ExerciseLog.class));
    }

    // ==================== WORKOUT HISTORY TESTS ====================

    @Test
    @DisplayName("getWorkoutHistory - Debería retornar historial")
    void getWorkoutHistory_Success() {

        when(workoutCompletionRepository.findByMember_IdAndDeletedAtIsNullOrderByCompletedAtDesc(1L))
                .thenReturn(Arrays.asList(testCompletion));
        when(workoutCompletionMapper.toDTO(any(WorkoutCompletion.class)))
                .thenReturn(testCompletionDTO);

        List<WorkoutCompletionDTO> result = workoutPlanService.getWorkoutHistory(1L);


        assertThat(result).hasSize(1);
        verify(workoutCompletionRepository).findByMember_IdAndDeletedAtIsNullOrderByCompletedAtDesc(1L);
    }

    @Test
    @DisplayName("getWorkoutHistoryByDateRange - Debería retornar historial filtrado")
    void getWorkoutHistoryByDateRange_Success() {

        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();

        when(workoutCompletionRepository.findByMemberAndDateRange(1L, start, end))
                .thenReturn(Arrays.asList(testCompletion));
        when(workoutCompletionMapper.toDTO(any(WorkoutCompletion.class)))
                .thenReturn(testCompletionDTO);

        List<WorkoutCompletionDTO> result = workoutPlanService.getWorkoutHistoryByDateRange(1L, start, end);


        assertThat(result).hasSize(1);
        verify(workoutCompletionRepository).findByMemberAndDateRange(1L, start, end);
    }

    // ==================== COUNT TESTS ====================

    @Test
    @DisplayName("countCompletedWorkouts - Debería retornar conteo")
    void countCompletedWorkouts_Success() {

        when(workoutCompletionRepository.countCompletionsByMember(1L)).thenReturn(10L);


        long result = workoutPlanService.countCompletedWorkouts(1L);

        assertThat(result).isEqualTo(10L);
        verify(workoutCompletionRepository).countCompletionsByMember(1L);
    }

    @Test
    @DisplayName("countActivePlans - Debería retornar conteo de planes activos")
    void countActivePlans_Success() {

        when(workoutPlanRepository.countActivePlansByTrainer(1L)).thenReturn(5L);

        long result = workoutPlanService.countActivePlans(1L);

        assertThat(result).isEqualTo(5L);
        verify(workoutPlanRepository).countActivePlansByTrainer(1L);
    }
}