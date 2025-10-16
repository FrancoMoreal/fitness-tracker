package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.member.RegisterMemberDTO;
import com.example.fitnesstracker.dto.request.member.UpdateMemberDTO;
import com.example.fitnesstracker.dto.response.MemberDTO;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.ResourceNotFoundException;
import com.example.fitnesstracker.exception.UserAlreadyExistsException;
import com.example.fitnesstracker.mapper.MemberMapper;
import com.example.fitnesstracker.model.Member;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.enums.UserRole;
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

    @Transactional
    public MemberDTO registerMember(RegisterMemberDTO dto) {
        log.info("Registrando nuevo miembro: {}", dto.getUsername());

        if (userRepository.existsByUsernameAndDeletedAtIsNull(dto.getUsername())) {
            throw new UserAlreadyExistsException("username", dto.getUsername());
        }

        if (userRepository.existsByEmailAndDeletedAtIsNull(dto.getEmail())) {
            throw new UserAlreadyExistsException("email", dto.getEmail());
        }

        if (memberRepository.existsByPhoneAndDeletedAtIsNull(dto.getPhone())) {
            throw new UserAlreadyExistsException("phone", dto.getPhone());
        }

        LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
        if (dto.getDateOfBirth().isAfter(eighteenYearsAgo)) {
            throw new InvalidUserDataException("dateOfBirth", "Debes tener al menos 18 años para registrarte");
        }

        User user = User.builder().username(dto.getUsername()).email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())).role(UserRole.USER).enabled(true).build();

        User savedUser = userRepository.save(user);
        log.debug("Usuario creado: {} (ID: {})", savedUser.getUsername(), savedUser.getId());

        LocalDate membershipStart = LocalDate.now();
        LocalDate membershipEndDate = membershipStart.plusYears(1);

        Member member = Member.builder().user(savedUser).firstName(dto.getFirstName()).lastName(dto.getLastName())
                .phone(dto.getPhone()).dateOfBirth(dto.getDateOfBirth()).membershipStartDate(membershipStart)
                .membershipEndDate(membershipEndDate).isActive(true).build();

        Member savedMember = memberRepository.save(member);
        savedUser.setMember(savedMember);
        userRepository.save(savedUser);

        log.info("Miembro registrado exitosamente: {} (ID: {})", savedMember.getFullName(), savedMember.getId());

        return memberMapper.toDTO(savedMember);
    }

    public MemberDTO getMemberById(Long memberId) {
        log.debug("Buscando miembro por ID: {}", memberId);
        return memberMapper.toDTO(findExistingMemberById(memberId));
    }

    public MemberDTO getMemberByExternalId(String externalId) {
        log.debug("Buscando miembro por externalId: {}", externalId);
        return memberMapper.toDTO(findExistingMemberByExternalId(externalId));
    }

    public List<MemberDTO> getAllMembers() {
        log.debug("Obteniendo todos los miembros activos");
        return memberRepository.findAll().stream().filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .map(memberMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public MemberDTO updateMember(Long memberId, UpdateMemberDTO dto) {
        log.info("Actualizando miembro: {}", memberId);

        Member member = findExistingMemberById(memberId);

        // Evitar NPE: primero verificar que dto.getPhone() no sea null
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
        memberRepository.save(member);
        userRepository.save(member.getUser());

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
        userRepository.save(member.getUser());

        log.info("Miembro restaurado exitosamente: {}", memberId);
    }

    public List<MemberDTO> getMembersByTrainer(Long trainerId) {
        log.debug("Obteniendo miembros del entrenador: {}", trainerId);
        return memberRepository.findByAssignedTrainer_IdAndDeletedAtIsNull(trainerId).stream().map(memberMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MemberDTO> getExpiredMemberships() {
        log.debug("Obteniendo membresías vencidas");
        return memberRepository.findExpiredMemberships(LocalDate.now()).stream().map(memberMapper::toDTO)
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
