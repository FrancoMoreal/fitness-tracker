package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.member.RegisterMemberDTO;
import com.example.fitnesstracker.dto.request.member.UpdateMemberDTO;
import com.example.fitnesstracker.dto.response.MemberDTO;
import com.example.fitnesstracker.enums.UserType;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.ResourceNotFoundException;
import com.example.fitnesstracker.exception.UserAlreadyExistsException;
import com.example.fitnesstracker.mapper.MemberMapper;
import com.example.fitnesstracker.model.Member;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.MemberRepository;
import com.example.fitnesstracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private static final String MEMBER_NOT_FOUND = "Miembro no encontrado";

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;
    private final UserService userService;

    @Transactional
    public MemberDTO registerMember(RegisterMemberDTO dto) {
        log.info("Registrando nuevo miembro: {}", dto.getUsername());

        // Validaciones
        userService.validateUniqueEmailAndUsername(dto.getUsername(), dto.getEmail());

        if (memberRepository.existsByPhoneAndDeletedAtIsNull(dto.getPhone())) {
            throw new UserAlreadyExistsException("phone", dto.getPhone());
        }

        LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
        if (dto.getDateOfBirth().isAfter(eighteenYearsAgo)) {
            throw new InvalidUserDataException("dateOfBirth", "Debes tener al menos 18 años para registrarte");
        }

        // CAMBIO 1: Crear user con UserType.MEMBER
        User savedUser = userService.createUserWithType(
                dto.getUsername(),
                dto.getEmail(),
                dto.getPassword(),
                UserType.MEMBER  // ← NUEVO
        );

        log.debug("Usuario creado: {} (ID: {})", savedUser.getUsername(), savedUser.getId());

        LocalDate membershipStart = LocalDate.now();
        LocalDate membershipEndDate = membershipStart.plusYears(1);

        // CAMBIO 2: Eliminar isActive (ahora usa BaseEntity.isActive())
        Member member = Member.builder()
                .user(savedUser)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .dateOfBirth(dto.getDateOfBirth())
                .membershipStartDate(membershipStart)
                .membershipEndDate(membershipEndDate)
                // .isActive(true) ← ELIMINAR, ya no existe
                .build();

        Member savedMember = memberRepository.save(member);

        // CAMBIO 3: La relación bidireccional se maneja automáticamente
        // savedUser.setMember(savedMember); ← ELIMINAR si no existe el setter
        // userRepository.save(savedUser); ← OPCIONAL, ya está guardado

        log.info("Miembro registrado exitosamente: {} (ID: {})", savedMember.getFullName(), savedMember.getId());

        return memberMapper.toDTO(savedMember);
    }

    // CAMBIO 4: Usar query optimizada con perfil completo
    public MemberDTO getMemberById(Long memberId) {
        log.debug("Buscando miembro por ID: {}", memberId);
        Member member = memberRepository.findByIdWithFullProfile(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(MEMBER_NOT_FOUND));
        return memberMapper.toDTO(member);
    }

    public MemberDTO getMemberByExternalId(String externalId) {
        log.debug("Buscando miembro por externalId: {}", externalId);
        return memberMapper.toDTO(findExistingMemberByExternalId(externalId));
    }

    // CAMBIO 5: Usar query optimizada para listar
    public List<MemberDTO> getAllMembers() {
        log.debug("Obteniendo todos los miembros activos");
        return memberRepository.findAllActiveWithValidMembership(LocalDate.now())
                .stream()
                .map(memberMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberDTO updateMember(Long memberId, UpdateMemberDTO dto) {
        log.info("Actualizando miembro: {}", memberId);

        Member member = findExistingMemberById(memberId);

        if (dto.getPhone() != null && !dto.getPhone().equals(member.getPhone())
                && memberRepository.existsByPhoneAndDeletedAtIsNull(dto.getPhone())) {
            throw new UserAlreadyExistsException("phone", dto.getPhone());
        }

        memberMapper.updateFromDTO(dto, member);
        Member updatedMember = memberRepository.save(member);

        log.info("Miembro actualizado exitosamente: {}", memberId);
        return memberMapper.toDTO(updatedMember);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        log.info("Eliminando miembro: {}", memberId);

        Member member = findExistingMemberById(memberId);

        member.softDelete();
        member.getUser().softDelete();

        // CAMBIO 6: Solo guardar member (cascade maneja user)
        memberRepository.save(member);
        // userRepository.save(member.getUser()); ← OPCIONAL, cascade lo hace

        log.info("Miembro eliminado exitosamente: {}", memberId);
    }

    @Transactional
    public void restoreMember(Long memberId) {
        log.info("Restaurando miembro: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(MEMBER_NOT_FOUND));

        member.restore();
        member.getUser().restore();
        memberRepository.save(member);

        log.info("Miembro restaurado exitosamente: {}", memberId);
    }

    // CAMBIO 7: Usar query optimizada con User cargado
    public List<MemberDTO> getMembersByTrainer(Long trainerId) {
        log.debug("Obteniendo miembros del entrenador: {}", trainerId);
        return memberRepository.findActiveByTrainerIdWithUser(trainerId)
                .stream()
                .map(memberMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MemberDTO> getExpiredMemberships() {
        log.debug("Obteniendo membresías vencidas");
        LocalDate today = LocalDate.now();
        LocalDate oneMonthAgo = today.minusMonths(1);
        return memberRepository.findMembersWithExpiringMembership(oneMonthAgo, today)
                .stream()
                .map(memberMapper::toDTO)
                .collect(Collectors.toList());
    }

    // NUEVO: Buscar members sin trainer
    public List<MemberDTO> getUnassignedMembers() {
        log.debug("Obteniendo miembros sin entrenador asignado");
        return memberRepository.findUnassignedActiveMembers(LocalDate.now())
                .stream()
                .map(memberMapper::toDTO)
                .collect(Collectors.toList());
    }

    // NUEVO: Buscar members por nombre
    public List<MemberDTO> searchMembers(String searchTerm) {
        log.debug("Buscando miembros por nombre: {}", searchTerm);
        return memberRepository.searchByName(searchTerm)
                .stream()
                .map(memberMapper::toDTO)
                .collect(Collectors.toList());
    }

    /* Helpers */
    private Member findExistingMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(MEMBER_NOT_FOUND));
        if (member.isDeleted()) {
            throw new ResourceNotFoundException(MEMBER_NOT_FOUND);
        }
        return member;
    }

    private Member findExistingMemberByExternalId(String externalId) {
        Member member = memberRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException(MEMBER_NOT_FOUND));
        if (member.isDeleted()) {
            throw new ResourceNotFoundException(MEMBER_NOT_FOUND);
        }
        return member;
    }
}
