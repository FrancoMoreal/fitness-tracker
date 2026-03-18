package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.nutrition.AddNutritionMealDTO;
import com.example.fitnesstracker.dto.request.nutrition.CreateNutritionPlanDTO;
import com.example.fitnesstracker.dto.response.nutrition.NutritionMealDTO;
import com.example.fitnesstracker.dto.response.nutrition.NutritionPlanDTO;
import com.example.fitnesstracker.enums.NutritionPlanStatus;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.ResourceNotFoundException;
import com.example.fitnesstracker.mapper.NutritionPlanMapper;
import com.example.fitnesstracker.model.Member;
import com.example.fitnesstracker.model.NutritionMeal;
import com.example.fitnesstracker.model.NutritionPlan;
import com.example.fitnesstracker.model.Trainer;
import com.example.fitnesstracker.repository.MemberRepository;
import com.example.fitnesstracker.repository.NutritionMealRepository;
import com.example.fitnesstracker.repository.NutritionPlanRepository;
import com.example.fitnesstracker.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionPlanService {

    private static final String PLAN_NOT_FOUND = "Plan nutricional no encontrado";

    private final NutritionPlanRepository nutritionPlanRepository;
    private final NutritionMealRepository nutritionMealRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final NutritionPlanMapper nutritionPlanMapper;

    @Transactional
    public NutritionPlanDTO createNutritionPlan(Long trainerId, CreateNutritionPlanDTO dto) {
        log.info("Trainer {} creando plan nutricional para member {}", trainerId, dto.getMemberId());

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

        if (member.getAssignedTrainer() == null || !member.getAssignedTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("Este miembro no está asignado a ti");
        }

        NutritionPlan plan = NutritionPlan.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .member(member)
                .trainer(trainer)
                .status(NutritionPlanStatus.DRAFT)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .notes(dto.getNotes())
                .build();

        NutritionPlan saved = nutritionPlanRepository.save(plan);
        log.info("Plan nutricional creado: {} (ID: {})", saved.getName(), saved.getId());
        return nutritionPlanMapper.toDTO(saved, false);
    }

    @Transactional
    public NutritionMealDTO addMeal(Long planId, Long trainerId, AddNutritionMealDTO dto) {
        log.info("Trainer {} agregando comida al plan {}", trainerId, planId);

        NutritionPlan plan = findExistingPlan(planId);

        if (!plan.getTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("No tenés permiso para modificar este plan");
        }

        NutritionMeal meal = NutritionMeal.builder()
                .nutritionPlan(plan)
                .mealType(dto.getMealType())
                .name(dto.getName())
                .calories(dto.getCalories())
                .protein(dto.getProtein())
                .carbs(dto.getCarbs())
                .fat(dto.getFat())
                .foods(dto.getFoods())
                .notes(dto.getNotes())
                .orderInPlan(dto.getOrderInPlan())
                .build();

        NutritionMeal saved = nutritionMealRepository.save(meal);
        plan.addMeal(saved);

        log.info("Comida agregada al plan: {}", saved.getName());
        return nutritionPlanMapper.toMealDTO(saved);
    }

    @Transactional
    public NutritionPlanDTO activatePlan(Long planId, Long trainerId) {
        log.info("Trainer {} activando plan nutricional {}", trainerId, planId);

        NutritionPlan plan = findExistingPlan(planId);

        if (!plan.getTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("No tenés permiso para activar este plan");
        }

        if (plan.getMeals() == null || plan.getMeals().isEmpty()) {
            throw new InvalidUserDataException("El plan debe tener al menos una comida");
        }

        plan.setStatus(NutritionPlanStatus.ACTIVE);
        NutritionPlan updated = nutritionPlanRepository.save(plan);
        log.info("Plan nutricional activado: {}", planId);
        return nutritionPlanMapper.toDTO(updated, true);
    }

    public NutritionPlanDTO getPlanById(Long planId) {
        return nutritionPlanMapper.toDTO(findExistingPlan(planId), true);
    }

    public List<NutritionPlanDTO> getActivePlansByMember(Long memberId) {
        return nutritionPlanRepository.findActivePlansByMember(memberId).stream()
                .map(p -> nutritionPlanMapper.toDTO(p, true))
                .collect(Collectors.toList());
    }

    public List<NutritionPlanDTO> getAllPlansByMember(Long memberId) {
        return nutritionPlanRepository.findByMember_IdAndDeletedAtIsNull(memberId).stream()
                .map(p -> nutritionPlanMapper.toDTO(p, false))
                .collect(Collectors.toList());
    }

    public List<NutritionPlanDTO> getPlansByTrainer(Long trainerId) {
        return nutritionPlanRepository.findByTrainer_IdAndDeletedAtIsNull(trainerId).stream()
                .map(p -> nutritionPlanMapper.toDTO(p, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePlan(Long planId, Long trainerId) {
        log.info("Trainer {} eliminando plan nutricional {}", trainerId, planId);

        NutritionPlan plan = findExistingPlan(planId);

        if (!plan.getTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("No tenés permiso para eliminar este plan");
        }

        plan.softDelete();
        nutritionPlanRepository.save(plan);
        log.info("Plan nutricional eliminado: {}", planId);
    }

    private NutritionPlan findExistingPlan(Long planId) {
        return nutritionPlanRepository.findById(planId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(PLAN_NOT_FOUND));
    }
    @Transactional
    public void removeMeal(Long mealId, Long trainerId) {
        log.info("Trainer {} eliminando comida {}", trainerId, mealId);
        NutritionMeal meal = nutritionMealRepository.findById(mealId)
                .filter(m -> !m.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Comida no encontrada"));
        if (!meal.getNutritionPlan().getTrainer().getId().equals(trainerId)) {
            throw new InvalidUserDataException("No tenés permiso para eliminar esta comida");
        }
        meal.softDelete();
        nutritionMealRepository.save(meal);
        log.info("Comida {} eliminada", mealId);
    }
}