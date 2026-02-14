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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService Unit Tests")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;


    @Mock
    private MemberMapper memberMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private MemberService memberService;

    private RegisterMemberDTO registerDto;
    private UpdateMemberDTO updateDto;
    private Member member;
    private User user;
    private MemberDTO memberDto;

    @BeforeEach
    void setUp() {
        registerDto = RegisterMemberDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phone("+123456789")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        updateDto = UpdateMemberDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("+987654321")
                .height(1.75)
                .weight(70.0)
                .build();

        user = spy(User.builder().id(1L).username("testuser").build());
        member = spy(Member.builder()
                .id(1L)
                .user(user)
                .firstName("John")
                .lastName("Doe")
                .phone("+123456789")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .membershipStartDate(LocalDate.now())
                .membershipEndDate(LocalDate.now().plusYears(1))
                .build());

        memberDto = MemberDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phone("+123456789")
                .build();
    }

    // ==================== REGISTER MEMBER TESTS ====================

    @Test
    @DisplayName("registerMember - Debería registrar miembro exitosamente")
    void registerMember_Success() {
        when(userService.createUserWithType(anyString(), anyString(), anyString(), eq(UserType.MEMBER))).thenReturn(user);
        when(memberRepository.save(any(Member.class))).thenReturn(member);
        when(memberMapper.toDTO(member)).thenReturn(memberDto);

        MemberDTO result = memberService.registerMember(registerDto).getMember();

        assertThat(result).isEqualTo(memberDto);
        verify(memberRepository).save(any(Member.class));
        verify(userService).createUserWithType(anyString(), anyString(), anyString(), eq(UserType.MEMBER));
    }

    @Test
    @DisplayName("registerMember - Debería lanzar excepción con teléfono duplicado")
    void registerMember_DuplicatePhone() {
        when(memberRepository.existsByPhoneAndDeletedAtIsNull(anyString())).thenReturn(true);

        assertThatThrownBy(() -> memberService.registerMember(registerDto))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("registerMember - Debería lanzar excepción con edad menor a 18")
    void registerMember_Underage() {
        registerDto.setDateOfBirth(LocalDate.now().minusYears(17));

        assertThatThrownBy(() -> memberService.registerMember(registerDto))
                .isInstanceOf(InvalidUserDataException.class);
    }

    // ==================== GET MEMBERS TESTS ====================

    @Test
    @DisplayName("getMemberById - Debería retornar miembro por ID")
    void getMemberById_Success() {
        when(memberRepository.findByIdWithFullProfile(1L)).thenReturn(Optional.of(member));
        when(memberMapper.toDTO(member)).thenReturn(memberDto);

        MemberDTO result = memberService.getMemberById(1L);

        assertThat(result).isEqualTo(memberDto);
    }

    @Test
    @DisplayName("getMemberById - Debería lanzar excepción con ID inexistente")
    void getMemberById_NotFound() {
        when(memberRepository.findByIdWithFullProfile(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMemberById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getMemberByExternalId - Debería retornar miembro por UUID")
    void getMemberByExternalId_Success() {
        when(memberRepository.findByExternalId("ext123")).thenReturn(Optional.of(member));
        when(memberMapper.toDTO(member)).thenReturn(memberDto);

        MemberDTO result = memberService.getMemberByExternalId("ext123");

        assertThat(result).isEqualTo(memberDto);
    }

    @Test
    @DisplayName("getAllMembers - Debería retornar lista de miembros activos")
    void getAllMembers_Success() {
        when(memberRepository.findAllActiveWithValidMembership(any(LocalDate.class))).thenReturn(List.of(member));
        when(memberMapper.toDTO(member)).thenReturn(memberDto);

        List<MemberDTO> result = memberService.getAllMembers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(memberDto);
    }

    // ==================== UPDATE MEMBER TESTS ====================

    @Test
    @DisplayName("updateMember - Debería actualizar miembro exitosamente")
    void updateMember_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);
        when(memberMapper.toDTO(member)).thenReturn(memberDto);

        MemberDTO result = memberService.updateMember(1L, updateDto);

        assertThat(result).isEqualTo(memberDto);
        verify(memberMapper).updateFromDTO(updateDto, member);
    }

    @Test
    @DisplayName("updateMember - Debería lanzar excepción con teléfono duplicado")
    void updateMember_DuplicatePhone() {
        updateDto.setPhone("+111111111");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.existsByPhoneAndDeletedAtIsNull(anyString())).thenReturn(true);

        assertThatThrownBy(() -> memberService.updateMember(1L, updateDto))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    // ==================== DELETE MEMBER TESTS ====================

    @Test
    @DisplayName("deleteMember - Debería hacer soft delete del miembro y usuario")
    void deleteMember_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);

        memberService.deleteMember(1L);

        verify(member).softDelete();
        verify(user).softDelete();
    }

    @Test
    @DisplayName("restoreMember - Debería restaurar miembro y usuario eliminado")
    void restoreMember_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);

        memberService.restoreMember(1L);

        verify(member).restore();
        verify(user).restore();
        verify(memberRepository).save(member);
    }

    // ==================== GET MEMBERS BY CRITERIA TESTS ====================

    @Test
    @DisplayName("getMembersByTrainer - Debería retornar lista de miembros por entrenador")
    void getMembersByTrainer_Success() {
        when(memberRepository.findActiveByTrainerIdWithUser(1L)).thenReturn(List.of(member));
        when(memberMapper.toDTO(member)).thenReturn(memberDto);

        List<MemberDTO> result = memberService.getMembersByTrainer(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(memberDto);
    }

    @Test
    @DisplayName("getExpiredMemberships - Debería retornar lista de membresías vencidas")
    void getExpiredMemberships_Success() {
        when(memberRepository.findMembersWithExpiringMembership(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(member));
        when(memberMapper.toDTO(member)).thenReturn(memberDto);

        List<MemberDTO> result = memberService.getExpiredMemberships();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(memberDto);
    }

    @Test
    @DisplayName("getUnassignedMembers - Debería retornar lista de miembros sin asignar")
    void getUnassignedMembers_Success() {
        when(memberRepository.findUnassignedActiveMembers(any(LocalDate.class))).thenReturn(List.of(member));
        when(memberMapper.toDTO(member)).thenReturn(memberDto);

        List<MemberDTO> result = memberService.getUnassignedMembers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(memberDto);
    }

    @Test
    @DisplayName("searchMembers - Debería buscar y retornar miembros por nombre")
    void searchMembers_Success() {
        when(memberRepository.searchByName("John")).thenReturn(List.of(member));
        when(memberMapper.toDTO(member)).thenReturn(memberDto);

        List<MemberDTO> result = memberService.searchMembers("John");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(memberDto);
    }
}
