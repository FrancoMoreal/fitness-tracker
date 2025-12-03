package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.MemberDTO;
import com.example.fitnesstracker.dto.response.TrainerDTO;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.enums.UserType;
import com.example.fitnesstracker.exception.UnauthorizedException;
import com.example.fitnesstracker.exception.UserNotFoundException;
import com.example.fitnesstracker.mapper.MemberMapper;
import com.example.fitnesstracker.mapper.TrainerMapper;
import com.example.fitnesstracker.mapper.UserMapper;
import com.example.fitnesstracker.model.Member;
import com.example.fitnesstracker.model.Trainer;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.UserRepository;
import com.example.fitnesstracker.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserDTO testUserDTO;
    private Member testMember;
    private MemberDTO testMemberDTO;
    private Trainer testTrainer;
    private TrainerDTO testTrainerDTO;

    @BeforeEach
    void setUp() {
        // Usuario base
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setEnabled(true);
        testUser.setRole(UserRole.USER);
        testUser.setUserType(UserType.NONE);

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setEnabled(true);
        testUserDTO.setRole(UserRole.USER);

        // Member
        testMember = new Member();
        testMember.setId(1L);
        testMember.setFirstName("John");
        testMember.setLastName("Doe");

        testMemberDTO = new MemberDTO();
        testMemberDTO.setId(1L);
        testMemberDTO.setFirstName("John");
        testMemberDTO.setLastName("Doe");

        // Trainer
        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setFirstName("Jane");
        testTrainer.setLastName("Smith");

        testTrainerDTO = new TrainerDTO();
        testTrainerDTO.setId(1L);
        testTrainerDTO.setFirstName("Jane");
        testTrainerDTO.setLastName("Smith");
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("login - Debería hacer login exitosamente con usuario básico")
    void login_Success() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        when(userRepository.findByUsernameAndDeletedAtIsNull(username))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken(username))
                .thenReturn(token);
        when(userMapper.toDto(testUser))
                .thenReturn(testUserDTO);


        AuthResponse result = authService.login(username, password);


        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(token);
        assertThat(result.getType()).isEqualTo("Bearer");
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getUsername()).isEqualTo(username);
        assertThat(result.getMember()).isNull();
        assertThat(result.getTrainer()).isNull();

        verify(userRepository).findByUsernameAndDeletedAtIsNull(username);
        verify(passwordEncoder).matches(password, testUser.getPassword());
        verify(jwtTokenProvider).generateToken(username);
    }

    @Test
    @DisplayName("login - Debería hacer login exitosamente con Member")
    void login_SuccessWithMember() {

        String username = "memberuser";
        String password = "password123";
        String token = "jwt.token.here";

        testUser.setUsername(username);
        testUser.setUserType(UserType.MEMBER);
        testUser.setMember(testMember);

        when(userRepository.findByUsernameAndDeletedAtIsNull(username))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken(username))
                .thenReturn(token);
        when(userMapper.toDto(testUser))
                .thenReturn(testUserDTO);
        when(memberMapper.toDTO(testMember))
                .thenReturn(testMemberDTO);


        AuthResponse result = authService.login(username, password);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(token);
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getMember()).isNotNull();
        assertThat(result.getMember().getFirstName()).isEqualTo("John");
        assertThat(result.getTrainer()).isNull();

        verify(memberMapper).toDTO(testMember);
    }

    @Test
    @DisplayName("login - Debería hacer login exitosamente con Trainer")
    void login_SuccessWithTrainer() {

        String username = "traineruser";
        String password = "password123";
        String token = "jwt.token.here";

        testUser.setUsername(username);
        testUser.setUserType(UserType.TRAINER);
        testUser.setTrainer(testTrainer);

        when(userRepository.findByUsernameAndDeletedAtIsNull(username))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken(username))
                .thenReturn(token);
        when(userMapper.toDto(testUser))
                .thenReturn(testUserDTO);
        when(trainerMapper.toDTO(testTrainer))
                .thenReturn(testTrainerDTO);


        AuthResponse result = authService.login(username, password);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(token);
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getMember()).isNull();
        assertThat(result.getTrainer()).isNotNull();
        assertThat(result.getTrainer().getFirstName()).isEqualTo("Jane");

        verify(trainerMapper).toDTO(testTrainer);
    }

    @Test
    @DisplayName("login - Debería lanzar excepción con usuario no encontrado")
    void login_UserNotFound() {

        String username = "noexiste";
        String password = "password123";

        when(userRepository.findByUsernameAndDeletedAtIsNull(username))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(username, password))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Usuario no encontrado");

        verify(userRepository).findByUsernameAndDeletedAtIsNull(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("login - Debería lanzar excepción con usuario deshabilitado")
    void login_UserDisabled() {

        String username = "testuser";
        String password = "password123";

        testUser.setEnabled(false);

        when(userRepository.findByUsernameAndDeletedAtIsNull(username))
                .thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(username, password))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Usuario deshabilitado");

        verify(userRepository).findByUsernameAndDeletedAtIsNull(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("login - Debería lanzar excepción con contraseña incorrecta")
    void login_WrongPassword() {
        // Arrange
        String username = "testuser";
        String password = "wrongpassword";

        when(userRepository.findByUsernameAndDeletedAtIsNull(username))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword()))
                .thenReturn(false);


        assertThatThrownBy(() -> authService.login(username, password))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciales inválidas");

        verify(userRepository).findByUsernameAndDeletedAtIsNull(username);
        verify(passwordEncoder).matches(password, testUser.getPassword());
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("login - Debería lanzar excepción con usuario eliminado (soft delete)")
    void login_DeletedUser() {

        String username = "deleteduser";
        String password = "password123";

        when(userRepository.findByUsernameAndDeletedAtIsNull(username))
                .thenReturn(Optional.empty()); // No encuentra usuarios con deletedAt != null

        assertThatThrownBy(() -> authService.login(username, password))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Usuario no encontrado");

        verify(userRepository).findByUsernameAndDeletedAtIsNull(username);
    }

    // ==================== VALIDATE TOKEN TESTS ====================

    @Test
    @DisplayName("validateTokenAndGetUser - Debería validar token y retornar usuario")
    void validateTokenAndGetUser_Success() {
        // Arrange
        String token = "valid.jwt.token";
        String username = "testuser";

        when(jwtTokenProvider.validateToken(token))
                .thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token))
                .thenReturn(username);
        when(userRepository.findByUsernameAndDeletedAtIsNull(username))
                .thenReturn(Optional.of(testUser));


        User result = authService.validateTokenAndGetUser(token);


        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getEnabled()).isTrue();

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).getUsernameFromToken(token);
        verify(userRepository).findByUsernameAndDeletedAtIsNull(username);
    }

    @Test
    @DisplayName("validateTokenAndGetUser - Debería lanzar excepción con token inválido")
    void validateTokenAndGetUser_InvalidToken() {
        // Arrange
        String token = "invalid.token";

        when(jwtTokenProvider.validateToken(token))
                .thenReturn(false);


        assertThatThrownBy(() -> authService.validateTokenAndGetUser(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Token inválido");

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
        verify(userRepository, never()).findByUsernameAndDeletedAtIsNull(anyString());
    }

    @Test
    @DisplayName("validateTokenAndGetUser - Debería lanzar excepción si usuario no existe")
    void validateTokenAndGetUser_UserNotFound() {

        String token = "valid.jwt.token";
        String username = "noexiste";

        when(jwtTokenProvider.validateToken(token))
                .thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token))
                .thenReturn(username);
        when(userRepository.findByUsernameAndDeletedAtIsNull(username))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.validateTokenAndGetUser(token))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Usuario no encontrado");

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).getUsernameFromToken(token);
        verify(userRepository).findByUsernameAndDeletedAtIsNull(username);
    }

    @Test
    @DisplayName("validateTokenAndGetUser - Debería lanzar excepción si usuario está deshabilitado")
    void validateTokenAndGetUser_UserDisabled() {

        String token = "valid.jwt.token";
        String username = "testuser";

        testUser.setEnabled(false);

        when(jwtTokenProvider.validateToken(token))
                .thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token))
                .thenReturn(username);
        when(userRepository.findByUsernameAndDeletedAtIsNull(username))
                .thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.validateTokenAndGetUser(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Usuario deshabilitado");

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).getUsernameFromToken(token);
        verify(userRepository).findByUsernameAndDeletedAtIsNull(username);
    }

    @Test
    @DisplayName("validateTokenAndGetUser - Debería lanzar excepción si usuario fue eliminado")
    void validateTokenAndGetUser_DeletedUser() {

        String token = "valid.jwt.token";
        String username = "deleteduser";

        when(jwtTokenProvider.validateToken(token))
                .thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token))
                .thenReturn(username);
        when(userRepository.findByUsernameAndDeletedAtIsNull(username))
                .thenReturn(Optional.empty()); // Usuario eliminado no se encuentra

        assertThatThrownBy(() -> authService.validateTokenAndGetUser(token))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Usuario no encontrado");

        verify(userRepository).findByUsernameAndDeletedAtIsNull(username);
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("login - Debería funcionar con username case-sensitive")
    void login_CaseSensitiveUsername() {

        String username = "TestUser"; // Mayúsculas
        String password = "password123";
        String token = "jwt.token";

        testUser.setUsername("TestUser");

        when(userRepository.findByUsernameAndDeletedAtIsNull("TestUser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken("TestUser"))
                .thenReturn(token);
        when(userMapper.toDto(testUser))
                .thenReturn(testUserDTO);

        AuthResponse result = authService.login(username, password);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(token);

        verify(userRepository).findByUsernameAndDeletedAtIsNull("TestUser");
    }

    @Test
    @DisplayName("login - Debería construir AuthResponse completo con Member y Trainer null")
    void login_AuthResponseStructure() {

        String username = "testuser";
        String password = "password123";
        String token = "jwt.token";

        when(userRepository.findByUsernameAndDeletedAtIsNull(username))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken(username))
                .thenReturn(token);
        when(userMapper.toDto(testUser))
                .thenReturn(testUserDTO);


        AuthResponse result = authService.login(username, password);


        assertThat(result.getToken()).isNotNull();
        assertThat(result.getType()).isEqualTo("Bearer");
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getMember()).isNull();
        assertThat(result.getTrainer()).isNull();
        assertThat(result.getExpiresAt()).isNull();
        assertThat(result.getRefreshToken()).isNull();
        assertThat(result.getMessage()).isNull();
    }
}