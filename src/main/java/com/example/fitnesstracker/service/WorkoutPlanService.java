package com.example.fitnesstracker.service;


import com.example.fitnesstracker.dto.request.workout.AddExerciseToWorkoutDTO;
import com.example.fitnesstracker.dto.request.workout.AddWorkoutDayDTO;
import com.example.fitnesstracker.dto.request.workout.CompleteWorkoutDTO;
import com.example.fitnesstracker.dto.request.workout.CreateWorkoutPlanDTO;
import com.example.fitnesstracker.dto.response.workout.*;
import com.example.fitnesstracker.enums.WorkoutPlanStatus;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.ResourceNotFoundException;
import com.example.fitnesstracker.mapper.*;
import com.example.fitnesstracker.model.*;
import com.example.fitnesstracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutCompletionRepository workoutCompletionRepository;
    private final ExerciseLogRepository exerciseLogRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final ExerciseRepository exerciseRepository;

    private final WorkoutPlanMapper workoutPlanMapper;
    private final WorkoutDayMapper workoutDayMapper;
    private final WorkoutExerciseMapper workoutExerciseMapper;
    private final WorkoutCompletionMapper workoutCompletionMapper;

    @Transactional
    public WorkoutPlanDTO createWorkoutPlan(Long trainerId, CreateWorkoutPlanDTO dto) {
        log.info("Trainer {} creando plan de workout para member {}", trainerId, dto.getMemberId());

        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrenador no encontrado"));

        if (trainer.isDeleted() || !trainer.getIsActive()) {
            throw new ResourceNotFoundException("Entrenador no disponible");
        }

        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Miembro no encontrado"));

        if (member.isDeleted()) {
            throw new ResourceNotFoundException("Miembro no encontrado");
        }

        // Validar que el trainer sea el asignado al member
        if (member.getAssignedTrainer() == null || !member.getAssignedTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("Este miembro no está asignado a ti");
        }

        WorkoutPlan plan = WorkoutPlan.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .member(member)
                .trainer(trainer)
                .status(WorkoutPlanStatus.DRAFT)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .notes(dto.getNotes())
                .build();

        WorkoutPlan savedPlan = workoutPlanRepository.save(plan);
        log.info("Plan de workout creado: {} (ID: {})", savedPlan.getName(), savedPlan.getId());

        return workoutPlanMapper.toDTO(savedPlan, false);
    }

    @Transactional
    public WorkoutDayDTO addWorkoutDay(Long planId, Long trainerId, AddWorkoutDayDTO dto) {
        log.info("Trainer {} agregando día al plan {}", trainerId, planId);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de workout no encontrado"));

        if (plan.isDeleted()) {
            throw new ResourceNotFoundException("Plan de workout no encontrado");
        }

        // Validar que el trainer sea el creador del plan
        if (!plan.getTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("No tienes permiso para modificar este plan");
        }

        WorkoutDay day = WorkoutDay.builder()
                .workoutPlan(plan)
                .dayName(dto.getDayName())
                .dayNumber(dto.getDayNumber())
                .notes(dto.getNotes())
                .build();

        WorkoutDay savedDay = workoutDayRepository.save(day);
        plan.addWorkoutDay(savedDay);

        log.info("Día agregado al plan: {}", savedDay.getDayName());
        return workoutDayMapper.toDTO(savedDay, false);
    }

    @Transactional
    public WorkoutExerciseDTO addExerciseToDay(Long dayId, Long trainerId, AddExerciseToWorkoutDTO dto) {
        log.info("Trainer {} agregando ejercicio al día {}", trainerId, dayId);

        WorkoutDay day = workoutDayRepository.findById(dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Día de workout no encontrado"));

        if (day.isDeleted()) {
            throw new ResourceNotFoundException("Día de workout no encontrado");
        }

        // Validar que el trainer sea el creador del plan
        if (!day.getWorkoutPlan().getTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("No tienes permiso para modificar este día");
        }

        Exercise exercise = exerciseRepository.findById(dto.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

        if (exercise.isDeleted()) {
            throw new ResourceNotFoundException("Ejercicio no encontrado");
        }

        WorkoutExercise workoutExercise = WorkoutExercise.builder()
                .workoutDay(day)
                .exercise(exercise)
                .sets(dto.getSets())
                .reps(dto.getReps())
                .weight(dto.getWeight())
                .restSeconds(dto.getRestSeconds())
                .orderInWorkout(dto.getOrderInWorkout())
                .notes(dto.getNotes())
                .build();

        WorkoutExercise savedExercise = workoutExerciseRepository.save(workoutExercise);
        day.addExercise(savedExercise);

        log.info("Ejercicio agregado al día: {}", exercise.getName());
        return workoutExerciseMapper.toDTO(savedExercise);
    }

    @Transactional
    public WorkoutPlanDTO activateWorkoutPlan(Long planId, Long trainerId) {
        log.info("Trainer {} activando plan {}", trainerId, planId);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de workout no encontrado"));

        if (plan.isDeleted()) {
            throw new ResourceNotFoundException("Plan de workout no encontrado");
        }

        // Validar que el trainer sea el creador
        if (!plan.getTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("No tienes permiso para activar este plan");
        }

        // Validar que el plan tenga al menos un día
        if (plan.getWorkoutDays() == null || plan.getWorkoutDays().isEmpty()) {
            throw new InvalidUserDataException("El plan debe tener al menos un día de entrenamiento");
        }

        plan.setStatus(WorkoutPlanStatus.ACTIVE);
        WorkoutPlan updatedPlan = workoutPlanRepository.save(plan);

        log.info("Plan activado: {}", planId);
        return workoutPlanMapper.toDTO(updatedPlan, true);
    }

    public WorkoutPlanDTO getWorkoutPlanById(Long planId) {
        log.debug("Obteniendo plan de workout: {}", planId);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de workout no encontrado"));

        if (plan.isDeleted()) {
            throw new ResourceNotFoundException("Plan de workout no encontrado");
        }

        return workoutPlanMapper.toDTO(plan, true);
    }

    public List<WorkoutPlanDTO> getWorkoutPlansByMember(Long memberId) {
        log.debug("Obteniendo planes del member: {}", memberId);

        return workoutPlanRepository.findByMember_IdAndDeletedAtIsNull(memberId).stream()
                .map(plan -> workoutPlanMapper.toDTO(plan, false))
                .collect(Collectors.toList());
    }

    public List<WorkoutPlanDTO> getActiveWorkoutPlansByMember(Long memberId) {
        log.debug("Obteniendo planes activos del member: {}", memberId);

        return workoutPlanRepository.findActivePlansByMember(memberId).stream()
                .map(plan -> workoutPlanMapper.toDTO(plan, true))
                .collect(Collectors.toList());
    }

    public List<WorkoutPlanDTO> getWorkoutPlansByTrainer(Long trainerId) {
        log.debug("Obteniendo planes del trainer: {}", trainerId);

        return workoutPlanRepository.findByTrainer_IdAndDeletedAtIsNull(trainerId).stream()
                .map(plan -> workoutPlanMapper.toDTO(plan, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteWorkoutPlan(Long planId, Long trainerId) {
        log.info("Trainer {} eliminando plan {}", trainerId, planId);

        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de workout no encontrado"));

        // Validar que el trainer sea el creador
        if (!plan.getTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("No tienes permiso para eliminar este plan");
        }

        plan.softDelete();
        workoutPlanRepository.save(plan);

        log.info("Plan eliminado: {}", planId);
    }

// member completa un workout day
    @Transactional
    public WorkoutCompletionDTO completeWorkout(Long memberId, Long dayId, CompleteWorkoutDTO dto) {
        log.info("Member {} completando workout day {}", memberId, dayId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Miembro no encontrado"));

        if (member.isDeleted()) {
            throw new ResourceNotFoundException("Miembro no encontrado");
        }

        WorkoutDay day = workoutDayRepository.findById(dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Día de workout no encontrado"));

        if (day.isDeleted()) {
            throw new ResourceNotFoundException("Día de workout no encontrado");
        }

        // Validar que el plan sea del member
        if (!day.getWorkoutPlan().getMember().getId().equals(memberId)) {
            throw new InvalidUserDataException("Este workout no es tuyo");
        }

        WorkoutCompletion completion = WorkoutCompletion.builder()
                .member(member)
                .workoutDay(day)
                .completedAt(LocalDate.now())
                .rating(dto.getRating())
                .notes(dto.getNotes())
                .build();

        WorkoutCompletion savedCompletion = workoutCompletionRepository.save(completion);

        // Guardar logs de ejercicios
        if (dto.getExerciseLogs() != null) {
            for (CompleteWorkoutDTO.ExerciseLogDTO logDTO : dto.getExerciseLogs()) {
                WorkoutExercise workoutExercise = workoutExerciseRepository.findById(logDTO.getWorkoutExerciseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ejercicio del workout no encontrado"));

                ExerciseLog log = ExerciseLog.builder()
                        .workoutCompletion(savedCompletion)
                        .workoutExercise(workoutExercise)
                        .setsCompleted(logDTO.getSetsCompleted())
                        .repsCompleted(logDTO.getRepsCompleted())
                        .weightUsed(logDTO.getWeightUsed())
                        .notes(logDTO.getNotes())
                        .build();

                exerciseLogRepository.save(log);
                savedCompletion.addExerciseLog(log);
            }
        }

        log.info("Workout completado: member {} - day {}", memberId, dayId);
        return workoutCompletionMapper.toDTO(savedCompletion);
    }

    public List<WorkoutCompletionDTO> getWorkoutHistory(Long memberId) {
        log.debug("Obteniendo historial de workouts del member: {}", memberId);

        return workoutCompletionRepository.findByMember_IdAndDeletedAtIsNullOrderByCompletedAtDesc(memberId).stream()
                .map(workoutCompletionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<WorkoutCompletionDTO> getWorkoutHistoryByDateRange(Long memberId, LocalDate startDate, LocalDate endDate) {
        log.debug("Obteniendo historial de workouts del member {} entre {} y {}", memberId, startDate, endDate);

        return workoutCompletionRepository.findByMemberAndDateRange(memberId, startDate, endDate).stream()
                .map(workoutCompletionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public long countCompletedWorkouts(Long memberId) {
        return workoutCompletionRepository.countCompletionsByMember(memberId);
    }

    public long countActivePlans(Long trainerId) {
        return workoutPlanRepository.countActivePlansByTrainer(trainerId);
    }
}