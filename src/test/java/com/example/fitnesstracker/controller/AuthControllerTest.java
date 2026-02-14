package com.example.fitnesstracker.controller;

import com.example.fitnesstracker.dto.request.UserLoginDTO;
import com.example.fitnesstracker.dto.request.member.RegisterMemberDTO;
import com.example.fitnesstracker.dto.request.trainer.RegisterTrainerDTO;
import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.MemberDTO;
import com.example.fitnesstracker.dto.response.TrainerDTO;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.UnauthorizedException;
import com.example.fitnesstracker.exception.UserAlreadyExistsException;
import com.example.fitnesstracker.security.JwtTokenProvider;
import com.example.fitnesstracker.service.AuthService;
import com.example.fitnesstracker.service.MemberService;
import com.example.fitnesstracker.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({TestSecurityConfig.class})
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private TrainerService trainerService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private com.example.fitnesstracker.security.CustomUserDetailsService customUserDetailsService;

    private UserDTO testUserDTO;
    private MemberDTO testMemberDTO;
    private TrainerDTO testTrainerDTO;
    private AuthResponse testAuthResponse;
    private UserLoginDTO loginDTO;
    private RegisterMemberDTO registerMemberDTO;
    private RegisterTrainerDTO registerTrainerDTO;

    @BeforeEach
    void setUp() {
        testUserDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .enabled(true)
                .role(UserRole.USER)
                .build();

        testMemberDTO = MemberDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .isActive(true)
                .build();

        testTrainerDTO = TrainerDTO.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .fullName("Jane Smith")
                .specialty("Strength Training")
                .certifications(Arrays.asList("NASM-CPT", "CSCS"))
                .hourlyRate(new BigDecimal("50.00"))
                .isActive(true)
                .build();

        testAuthResponse = AuthResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .type("Bearer")
                .user(testUserDTO)
                .build();

        loginDTO = UserLoginDTO.builder()
                .username("testuser")
                .password("password123")
                .build();

        registerMemberDTO = RegisterMemberDTO.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        registerTrainerDTO = RegisterTrainerDTO.builder()
                .username("janesmith")
                .email("jane@example.com")
                .password("SecurePass123!")
                .firstName("Jane")
                .lastName("Smith")
                .specialty("Strength Training")
                .certifications(Arrays.asList("NASM-CPT", "CSCS"))
                .hourlyRate(new BigDecimal("50.00"))
                .build();
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("POST /auth/login - Debería hacer login exitosamente")
    void login_Success() throws Exception {
        when(authService.login(anyString(), anyString())).thenReturn(testAuthResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")))
                .andExpect(jsonPath("$.type", is("Bearer")))
                .andExpect(jsonPath("$.user.username", is("testuser")))
                .andExpect(jsonPath("$.user.email", is("test@example.com")));

        verify(authService).login("testuser", "password123");
    }

    @Test
    @DisplayName("POST /auth/login - Debería retornar 401 con credenciales inválidas")
    void login_InvalidCredentials() throws Exception {
        when(authService.login(anyString(), anyString()))
                .thenThrow(new UnauthorizedException("Credenciales inválidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());

        verify(authService).login("testuser", "password123");
    }

    @Test
    @DisplayName("POST /auth/login - Debería retornar 401 con usuario deshabilitado")
    void login_UserDisabled() throws Exception {
        when(authService.login(anyString(), anyString()))
                .thenThrow(new UnauthorizedException("Usuario deshabilitado"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login - Debería retornar 400 sin username")
    void login_MissingUsername() throws Exception {
        String jsonWithoutUsername = """
                {
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutUsername))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /auth/login - Debería retornar 400 sin password")
    void login_MissingPassword() throws Exception {
        String jsonWithoutPassword = """
                {
                    "username": "testuser"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutPassword))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /auth/login - Debería manejar JSON malformado")
    void login_MalformedJson() throws Exception {
        String malformedJson = "{ username: testuser, password: }";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(anyString(), anyString());
    }

    // ==================== REGISTER MEMBER TESTS ====================

    @Test
    @DisplayName("POST /auth/register/member - Debería registrar member exitosamente")
    void registerMember_Success() throws Exception {
        AuthResponse memberAuthResponse = AuthResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .type("Bearer")
                .user(testUserDTO)
                .member(testMemberDTO)
                .message("Member registrado exitosamente")
                .build();

        when(memberService.registerMember(any(RegisterMemberDTO.class)))
                .thenReturn(memberAuthResponse);

        mockMvc.perform(post("/auth/register/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerMemberDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.fullName", is("John Doe")))
                .andExpect(jsonPath("$.phone", is("+1234567890")))
                .andExpect(jsonPath("$.isActive", is(true)));

        verify(memberService).registerMember(any(RegisterMemberDTO.class));
    }

    @Test
    @DisplayName("POST /auth/register/member - Debería retornar 409 con username duplicado")
    void registerMember_DuplicateUsername() throws Exception {
        when(memberService.registerMember(any(RegisterMemberDTO.class)))
                .thenThrow(new UserAlreadyExistsException("username", "El nombre de usuario ya está registrado"));

        mockMvc.perform(post("/auth/register/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerMemberDTO)))
                .andExpect(status().isConflict());

        verify(memberService).registerMember(any(RegisterMemberDTO.class));
    }

    @Test
    @DisplayName("POST /auth/register/member - Debería retornar 409 con email duplicado")
    void registerMember_DuplicateEmail() throws Exception {
        when(memberService.registerMember(any(RegisterMemberDTO.class)))
                .thenThrow(new UserAlreadyExistsException("email", "El email ya está registrado"));

        mockMvc.perform(post("/auth/register/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerMemberDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /auth/register/member - Debería retornar 400 con edad inválida")
    void registerMember_InvalidAge() throws Exception {
        RegisterMemberDTO invalidAgeDTO = RegisterMemberDTO.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.now().minusYears(10))
                .build();

        when(memberService.registerMember(any(RegisterMemberDTO.class)))
                .thenThrow(new InvalidUserDataException("dateOfBirth", "Debe ser mayor de 18 años"));

        mockMvc.perform(post("/auth/register/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAgeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register/member - Debería retornar 400 con teléfono duplicado")
    void registerMember_DuplicatePhone() throws Exception {
        when(memberService.registerMember(any(RegisterMemberDTO.class)))
                .thenThrow(new InvalidUserDataException("phone", "El teléfono ya está registrado"));

        mockMvc.perform(post("/auth/register/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerMemberDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register/member - Debería validar campos requeridos")
    void registerMember_MissingRequiredFields() throws Exception {
        String incompleteJson = """
                {
                    "username": "johndoe"
                }
                """;

        mockMvc.perform(post("/auth/register/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(memberService, never()).registerMember(any());
    }

    @Test
    @DisplayName("POST /auth/register/member - Debería manejar errores internos del servicio")
    void registerMember_ServiceError() throws Exception {
        when(memberService.registerMember(any(RegisterMemberDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/auth/register/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerMemberDTO)))
                .andExpect(status().is5xxServerError());
    }

    // ==================== REGISTER TRAINER TESTS ====================

    @Test
    @DisplayName("POST /auth/register/trainer - Debería registrar trainer exitosamente")
    void registerTrainer_Success() throws Exception {
        AuthResponse trainerAuthResponse = AuthResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .type("Bearer")
                .user(testUserDTO)
                .trainer(testTrainerDTO)
                .message("Trainer registrado exitosamente")
                .build();

        when(trainerService.registerTrainer(any(RegisterTrainerDTO.class)))
                .thenReturn(trainerAuthResponse);

        mockMvc.perform(post("/auth/register/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerTrainerDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Smith")))
                .andExpect(jsonPath("$.fullName", is("Jane Smith")))
                .andExpect(jsonPath("$.specialty", is("Strength Training")))
                .andExpect(jsonPath("$.hourlyRate", is(50.00)))
                .andExpect(jsonPath("$.isActive", is(true)))
                .andExpect(jsonPath("$.certifications", notNullValue()));

        verify(trainerService).registerTrainer(any(RegisterTrainerDTO.class));
    }

    @Test
    @DisplayName("POST /auth/register/trainer - Debería retornar 409 con username duplicado")
    void registerTrainer_DuplicateUsername() throws Exception {
        when(trainerService.registerTrainer(any(RegisterTrainerDTO.class)))
                .thenThrow(new UserAlreadyExistsException("username", "El nombre de usuario ya está registrado"));

        mockMvc.perform(post("/auth/register/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerTrainerDTO)))
                .andExpect(status().isConflict());

        verify(trainerService).registerTrainer(any(RegisterTrainerDTO.class));
    }

    @Test
    @DisplayName("POST /auth/register/trainer - Debería retornar 400 con tarifa inválida")
    void registerTrainer_InvalidHourlyRate() throws Exception {
        RegisterTrainerDTO invalidDTO = RegisterTrainerDTO.builder()
                .username("janesmith")
                .email("jane@example.com")
                .password("SecurePass123!")
                .firstName("Jane")
                .lastName("Smith")
                .specialty("Strength")
                .certifications(Arrays.asList("NASM-CPT"))
                .hourlyRate(new BigDecimal("-10"))
                .build();

        when(trainerService.registerTrainer(any(RegisterTrainerDTO.class)))
                .thenThrow(new InvalidUserDataException("hourlyRate", "Tarifa horaria debe ser mayor a 0"));

        mockMvc.perform(post("/auth/register/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register/trainer - Debería validar certifications no vacías")
    void registerTrainer_EmptyCertifications() throws Exception {
        String jsonWithEmptyCerts = """
                {
                    "username": "janesmith",
                    "email": "jane@example.com",
                    "password": "SecurePass123!",
                    "firstName": "Jane",
                    "lastName": "Smith",
                    "specialty": "Strength",
                    "certifications": [],
                    "hourlyRate": 50.00
                }
                """;

        mockMvc.perform(post("/auth/register/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithEmptyCerts))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).registerTrainer(any());
    }

    @Test
    @DisplayName("POST /auth/register/trainer - Debería validar formato de password")
    void registerTrainer_WeakPassword() throws Exception {
        RegisterTrainerDTO weakPasswordDTO = RegisterTrainerDTO.builder()
                .username("janesmith")
                .email("jane@example.com")
                .password("123")
                .firstName("Jane")
                .lastName("Smith")
                .specialty("Strength")
                .certifications(Arrays.asList("NASM-CPT"))
                .hourlyRate(new BigDecimal("50.00"))
                .build();

        mockMvc.perform(post("/auth/register/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(weakPasswordDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).registerTrainer(any());
    }

    @Test
    @DisplayName("POST /auth/register/trainer - Debería validar campos requeridos")
    void registerTrainer_MissingRequiredFields() throws Exception {
        String incompleteJson = """
                {
                    "username": "janesmith",
                    "email": "jane@example.com"
                }
                """;

        mockMvc.perform(post("/auth/register/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).registerTrainer(any());
    }

    @Test
    @DisplayName("POST /auth/register/trainer - Debería manejar errores internos del servicio")
    void registerTrainer_ServiceError() throws Exception {
        when(trainerService.registerTrainer(any(RegisterTrainerDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/auth/register/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerTrainerDTO)))
                .andExpect(status().is5xxServerError());
    }
}