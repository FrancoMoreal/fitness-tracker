package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.member.RegisterMemberDTO;
import com.example.fitnesstracker.dto.request.member.UpdateMemberDTO;
import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.MemberDTO;
import com.example.fitnesstracker.enums.AssignmentStatus;
import com.example.fitnesstracker.enums.UserType;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.ResourceNotFoundException;
import com.example.fitnesstracker.exception.UserAlreadyExistsException;
import com.example.fitnesstracker.mapper.MemberMapper;
import com.example.fitnesstracker.mapper.UserMapper;
import com.example.fitnesstracker.model.Member;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.MemberRepository;
import com.example.fitnesstracker.repository.UserRepository;
import com.example.fitnesstracker.security.JwtTokenProvider;
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
public class MemberService {

    private static final String MEMBER_NOT_FOUND = "Miembro no encontrado";
    private static final String PHONE_ALREADY_EXISTS = "El teléfono ya está registrado";
    private static final String UNDERAGE_ERROR = "Debes tener al menos 18 años para registrarte";

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final MemberMapper memberMapper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse registerMember(RegisterMemberDTO dto) {
        log.info("Registrando nuevo miembro: {}", dto.getUsername());

        // 1. Crear usuario con tipo MEMBER
        User user = userService.createUserWithType(
                dto.getUsername(),
                dto.getEmail(),
                dto.getPassword(),
                UserType.MEMBER
        );

        // 2. Crear perfil de miembro
        Member member = Member.builder()
                .user(user)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .dateOfBirth(dto.getDateOfBirth())
                .membershipStartDate(LocalDate.now())
                .membershipEndDate(LocalDate.now().plusMonths(1))
                .assignmentStatus(AssignmentStatus.NO_TRAINER)
                .build();

        Member savedMember = memberRepository.save(member);

        // 3. Generar token
        String token = jwtTokenProvider.generateToken(user.getUsername());

        log.info("Miembro registrado: {}", user.getUsername());

        // 4. Retornar AuthResponse
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userMapper.toDto(user))
                .member(memberMapper.toDTO(savedMember))
                .message("Miembro registrado exitosamente")
                .build();
    }

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

        if (dto.getPhone() != null && !dto.getPhone().equals(member.getPhone())) {
            validateUniquePhone(dto.getPhone());
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

        memberRepository.save(member);
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

    public List<MemberDTO> getUnassignedMembers() {
        log.debug("Obteniendo miembros sin entrenador asignado");
        return memberRepository.findUnassignedActiveMembers(LocalDate.now())
                .stream()
                .map(memberMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MemberDTO> searchMembers(String searchTerm) {
        log.debug("Buscando miembros por nombre: {}", searchTerm);
        return memberRepository.searchByName(searchTerm)
                .stream()
                .map(memberMapper::toDTO)
                .collect(Collectors.toList());
    }

    /* Métodos privados reutilizables */
    private void validateUniquePhone(String phone) {
        if (memberRepository.existsByPhoneAndDeletedAtIsNull(phone)) {
            throw new UserAlreadyExistsException("phone", PHONE_ALREADY_EXISTS);
        }
    }

    private void validateAge(LocalDate dateOfBirth) {
        LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
        if (dateOfBirth.isAfter(eighteenYearsAgo)) {
            throw new InvalidUserDataException("dateOfBirth", UNDERAGE_ERROR);
        }
    }

    private Member createMember(RegisterMemberDTO dto, User savedUser) {
        LocalDate membershipStart = LocalDate.now();
        LocalDate membershipEndDate = membershipStart.plusYears(1);

        return Member.builder()
                .user(savedUser)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .dateOfBirth(dto.getDateOfBirth())
                .membershipStartDate(membershipStart)
                .membershipEndDate(membershipEndDate)
                .build();
    }

    private Member findExistingMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .filter(member -> !member.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(MEMBER_NOT_FOUND));
    }

    private Member findExistingMemberByExternalId(String externalId) {
        return memberRepository.findByExternalId(externalId)
                .filter(member -> !member.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(MEMBER_NOT_FOUND));
    }
}
