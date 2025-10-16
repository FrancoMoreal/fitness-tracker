package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.trainer.RegisterTrainerDTO;
import com.example.fitnesstracker.dto.request.trainer.UpdateTrainerDTO;
import com.example.fitnesstracker.dto.response.TrainerDTO;
import com.example.fitnesstracker.exception.ResourceNotFoundException;
import com.example.fitnesstracker.exception.UserAlreadyExistsException;
import com.example.fitnesstracker.mapper.TrainerMapper;
import com.example.fitnesstracker.model.Trainer;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.repository.TrainerRepository;
import com.example.fitnesstracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerService {

    private static final String TRAINER_NOT_FOUND = "Entrenador no encontrado";

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TrainerMapper trainerMapper;

    @Transactional
    public TrainerDTO registerTrainer(RegisterTrainerDTO dto) {
        log.info("Registrando nuevo entrenador: {}", dto.getUsername());

        // Validar que username no exista
        if (userRepository.existsByUsernameAndDeletedAtIsNull(dto.getUsername())) {
            throw new UserAlreadyExistsException("username", dto.getUsername());
        }

        // Validar que email no exista
        if (userRepository.existsByEmailAndDeletedAtIsNull(dto.getEmail())) {
            throw new UserAlreadyExistsException("email", dto.getEmail());
        }

        // Crear User
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.USER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.debug("Usuario creado: {} (ID: {})", savedUser.getUsername(), savedUser.getId());

        // Convertir certifications a String
        String certificationsStr = String.join(",", dto.getCertifications());

        // Crear Trainer
        Trainer trainer = Trainer.builder()
                .user(savedUser)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .specialty(dto.getSpecialty())
                .certifications(certificationsStr)
                .hourlyRate(dto.getHourlyRate())
                .isActive(true)
                .build();

        Trainer savedTrainer = trainerRepository.save(trainer);
        savedUser.setTrainer(savedTrainer);
        userRepository.save(savedUser);

        log.info("Entrenador registrado exitosamente: {} (ID: {})", savedTrainer.getFullName(), savedTrainer.getId());

        return trainerMapper.toDTO(savedTrainer);
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
        return trainerRepository.findAllActiveTrainers().stream()
                .map(trainerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<TrainerDTO> getAvailableTrainers() {
        log.debug("Obteniendo entrenadores disponibles (sin miembros asignados)");
        return trainerRepository.findAvailableTrainersWithNoMembers().stream()
                .map(trainerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainerDTO updateTrainer(Long trainerId, UpdateTrainerDTO dto) {
        log.info("Actualizando entrenador: {}", trainerId);

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

        // Soft delete
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
        return trainerRepository.findActiveTrainersBySpecialty(specialty).stream()
                .map(trainerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<TrainerDTO> getMostBusyTrainers() {
        log.debug("Obteniendo entrenadores más ocupados");
        return trainerRepository.findMostBusyTrainers().stream()
                .map(trainerMapper::toDTO)
                .collect(Collectors.toList());
    }

    /* Helpers */
    private Trainer findExistingTrainerById(Long trainerId) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException(TRAINER_NOT_FOUND));
        if (trainer.isDeleted()) {
            throw new ResourceNotFoundException(TRAINER_NOT_FOUND);
        }
        return trainer;
    }

    private Trainer findExistingTrainerByExternalId(String externalId) {
        Trainer trainer = trainerRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException(TRAINER_NOT_FOUND));
        if (trainer.isDeleted()) {
            throw new ResourceNotFoundException(TRAINER_NOT_FOUND);
        }
        return trainer;
    }
}
