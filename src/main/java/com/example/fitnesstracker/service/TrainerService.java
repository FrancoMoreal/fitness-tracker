package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.trainer.RegisterTrainerDTO;
import com.example.fitnesstracker.dto.request.trainer.UpdateTrainerDTO;
import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.TrainerDTO;
import com.example.fitnesstracker.enums.UserType;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.ResourceNotFoundException;
import com.example.fitnesstracker.mapper.TrainerMapper;
import com.example.fitnesstracker.mapper.UserMapper;
import com.example.fitnesstracker.model.Trainer;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.TrainerRepository;
import com.example.fitnesstracker.repository.UserRepository;
import com.example.fitnesstracker.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerService {

    private static final String TRAINER_NOT_FOUND = "Entrenador no encontrado";

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final TrainerMapper trainerMapper;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;


    @Transactional
    public AuthResponse registerTrainer(RegisterTrainerDTO dto) {
        log.info("Registrando nuevo entrenador: {}", dto.getUsername());

        // 1. Crear usuario con tipo TRAINER
        User user = userService.createUserWithType(
                dto.getUsername(),
                dto.getEmail(),
                dto.getPassword(),
                UserType.TRAINER
        );

        // 2. Crear perfil de trainer
        Trainer trainer = Trainer.builder()
                .user(user)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .specialty(dto.getSpecialty())
                .certifications(String.join(", ", dto.getCertifications()))
                .hourlyRate(dto.getHourlyRate())
                .isActive(true)
                .build();

        Trainer savedTrainer = trainerRepository.save(trainer);

        // 3. Generar token
        String token = jwtTokenProvider.generateToken(user.getUsername());

        log.info("Entrenador registrado: {}", user.getUsername());

        // 4. Retornar AuthResponse
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userMapper.toDto(user))
                .trainer(trainerMapper.toDTO(savedTrainer))
                .message("Entrenador registrado exitosamente")
                .build();
    }

    public TrainerDTO getTrainerById(Long trainerId) {
        log.debug("Buscando entrenador por ID: {}", trainerId);
        return trainerMapper.toDTO(findExistingTrainerById(trainerId));
    }

    public TrainerDTO getTrainerByExternalId(String externalId) {
        log.debug("Buscando entrenador por externalId: {}", externalId);
        return trainerMapper.toDTO(findExistingTrainerByExternalId(externalId));
    }

    public List<TrainerDTO> getAllTrainers() {
        log.debug("Obteniendo todos los entrenadores activos");
        return trainerRepository.findAllActiveTrainers()
                .stream()
                .map(trainerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<TrainerDTO> getAvailableTrainers() {
        log.debug("Obteniendo entrenadores disponibles (sin miembros asignados)");
        return trainerRepository.findAvailableTrainersWithNoMembers()
                .stream()
                .map(trainerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainerDTO updateTrainer(Long trainerId, UpdateTrainerDTO dto) {
        log.info("Actualizando entrenador: {}", trainerId);

        validateHourlyRate(dto.getHourlyRate());
        Trainer trainer = findExistingTrainerById(trainerId);
        trainerMapper.updateFromDTO(dto, trainer);

        Trainer updatedTrainer = trainerRepository.save(trainer);
        log.info("Entrenador actualizado exitosamente: {}", trainerId);
        return trainerMapper.toDTO(updatedTrainer);
    }

    @Transactional
    public void deleteTrainer(Long trainerId) {
        log.info("Eliminando entrenador: {}", trainerId);

        Trainer trainer = findExistingTrainerById(trainerId);
        trainer.softDelete();
        trainer.getUser().softDelete();

        trainerRepository.save(trainer);
        userRepository.save(trainer.getUser());
        log.info("Entrenador eliminado exitosamente: {}", trainerId);
    }

    @Transactional
    public void restoreTrainer(Long trainerId) {
        log.info("Restaurando entrenador: {}", trainerId);

        Trainer trainer = findExistingTrainerById(trainerId);
        trainer.restore();
        trainer.getUser().restore();

        trainerRepository.save(trainer);
        userRepository.save(trainer.getUser());
        log.info("Entrenador restaurado exitosamente: {}", trainerId);
    }

    public List<TrainerDTO> searchTrainersBySpecialty(String specialty) {
        log.debug("Buscando entrenadores por especialidad: {}", specialty);
        return trainerRepository.findActiveTrainersBySpecialty(specialty)
                .stream()
                .map(trainerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<TrainerDTO> getMostBusyTrainers() {
        log.debug("Obteniendo entrenadores más ocupados");
        return trainerRepository.findMostBusyTrainers()
                .stream()
                .map(trainerMapper::toDTO)
                .collect(Collectors.toList());
    }

    /* Métodos privados reutilizables */
    private void validateHourlyRate(BigDecimal hourlyRate) {
        if (hourlyRate == null || hourlyRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidUserDataException("Tarifa horaria debe ser mayor a 0");
        }
    }

    private Trainer createTrainer(RegisterTrainerDTO dto, User user) {
        String certificationsStr = String.join(",", dto.getCertifications());
        return trainerRepository.save(Trainer.builder()
                .user(user)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .specialty(dto.getSpecialty())
                .certifications(certificationsStr)
                .hourlyRate(dto.getHourlyRate())
                .isActive(true)
                .build());
    }

    private Trainer findExistingTrainerById(Long trainerId) {
        return trainerRepository.findById(trainerId)
                .filter(trainer -> !trainer.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(TRAINER_NOT_FOUND));
    }

    private Trainer findExistingTrainerByExternalId(String externalId) {
        return trainerRepository.findByExternalId(externalId)
                .filter(trainer -> !trainer.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(TRAINER_NOT_FOUND));
    }
}
