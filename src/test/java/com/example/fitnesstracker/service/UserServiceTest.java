package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.UserAlreadyExistsException;
import com.example.fitnesstracker.exception.UserNotFoundException;
import com.example.fitnesstracker.mapper.UserMapper;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private UserRegisterDTO testRegisterDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setExternalId("550e8400-e29b-41d4-a716-446655440000");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
        testUser.setRole(UserRole.USER);

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setExternalId("550e8400-e29b-41d4-a716-446655440000");
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setEnabled(true);
        testUserDTO.setRole(UserRole.USER);

        testRegisterDTO = new UserRegisterDTO();
        testRegisterDTO.setUsername("newuser");
        testRegisterDTO.setEmail("new@example.com");
        testRegisterDTO.setPassword("password123");
    }

    // ==================== REGISTER USER TESTS ====================

    @Test
    @DisplayName("registerUser - Debería registrar usuario exitosamente")
    void registerUser_Success() {
        // Arrange
        when(userRepository.existsByUsernameAndDeletedAtIsNull("newuser")).thenReturn(false);
        when(userRepository.existsByEmailAndDeletedAtIsNull("new@example.com")).thenReturn(false);
        when(userMapper.toEntity(any(UserRegisterDTO.class))).thenReturn(testUser);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

        UserDTO result = userService.registerUser(testRegisterDTO);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).existsByUsernameAndDeletedAtIsNull("newuser");
        verify(userRepository).existsByEmailAndDeletedAtIsNull("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con username vacío")
    void registerUser_EmptyUsername() {

        testRegisterDTO.setUsername("");


        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("username")
                .hasMessageContaining("obligatorio");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con username null")
    void registerUser_NullUsername() {

        testRegisterDTO.setUsername(null);


        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("username");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con email vacío")
    void registerUser_EmptyEmail() {

        testRegisterDTO.setEmail("");


        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("email")
                .hasMessageContaining("obligatorio");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con email null")
    void registerUser_NullEmail() {

        testRegisterDTO.setEmail(null);


        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con contraseña vacía")
    void registerUser_EmptyPassword() {

        testRegisterDTO.setPassword("");


        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("contraseña")
                .hasMessageContaining("obligatoria");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con contraseña corta")
    void registerUser_PasswordTooShort() {

        testRegisterDTO.setPassword("123");


        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("contraseña")
                .hasMessageContaining("6 caracteres");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con username duplicado")
    void registerUser_DuplicateUsername() {

        when(userRepository.existsByUsernameAndDeletedAtIsNull("newuser")).thenReturn(true);


        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("nombre de usuario");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con email duplicado")
    void registerUser_DuplicateEmail() {

        when(userRepository.existsByUsernameAndDeletedAtIsNull("newuser")).thenReturn(false);
        when(userRepository.existsByEmailAndDeletedAtIsNull("new@example.com")).thenReturn(true);


        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("login - Debería hacer login exitosamente")
    void login_Success() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(username)).thenReturn(token);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);


        AuthResponse result = userService.login(username, password);


        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(token);
        assertThat(result.getUser().getUsername()).isEqualTo(username);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(username);
    }

    @Test
    @DisplayName("login - Debería lanzar excepción con username vacío")
    void login_EmptyUsername() {

        assertThatThrownBy(() -> userService.login("", "password123"))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("username");

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("login - Debería lanzar excepción con password vacío")
    void login_EmptyPassword() {

        assertThatThrownBy(() -> userService.login("testuser", ""))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("contraseña");

        verify(authenticationManager, never()).authenticate(any());
    }

    // ==================== GET USERS TESTS ====================

    @Test
    @DisplayName("getAllUsers - Debería retornar lista de usuarios activos")
    void getAllUsers_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2L);
        userDTO2.setUsername("user2");

        when(userRepository.findAllActive()).thenReturn(Arrays.asList(testUser, user2));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);
        when(userMapper.toDto(user2)).thenReturn(userDTO2);


        List<UserDTO> result = userService.getAllUsers();


        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDTO::getUsername)
                .containsExactly("testuser", "user2");
        verify(userRepository).findAllActive();
    }

    @Test
    @DisplayName("getAllUsers - Debería retornar lista vacía si no hay usuarios")
    void getAllUsers_EmptyList() {

        when(userRepository.findAllActive()).thenReturn(List.of());


        List<UserDTO> result = userService.getAllUsers();


        assertThat(result).isEmpty();
        verify(userRepository).findAllActive();
    }

    @Test
    @DisplayName("getUserById - Debería retornar usuario por ID")
    void getUserById_Success() {

        when(userRepository.findByIdActive(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);


        UserDTO result = userService.getUserById(1L);


        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findByIdActive(1L);
    }

    @Test
    @DisplayName("getUserById - Debería lanzar excepción con ID inexistente")
    void getUserById_NotFound() {

        when(userRepository.findByIdActive(999L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Usuario no encontrado");

        verify(userRepository).findByIdActive(999L);
    }

    @Test
    @DisplayName("getUserByExternalId - Debería retornar usuario por UUID")
    void getUserByExternalId_Success() {

        String externalId = "550e8400-e29b-41d4-a716-446655440000";
        when(userRepository.findByExternalIdAndDeletedAtIsNull(externalId)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);


        UserDTO result = userService.getUserByExternalId(externalId);


        assertThat(result).isNotNull();
        assertThat(result.getExternalId()).isEqualTo(externalId);
        verify(userRepository).findByExternalIdAndDeletedAtIsNull(externalId);
    }

    @Test
    @DisplayName("getUserByExternalId - Debería lanzar excepción con UUID inexistente")
    void getUserByExternalId_NotFound() {

        String externalId = "invalid-uuid";
        when(userRepository.findByExternalIdAndDeletedAtIsNull(externalId)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.getUserByExternalId(externalId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByExternalIdAndDeletedAtIsNull(externalId);
    }

    @Test
    @DisplayName("getUsersByRole - Debería filtrar usuarios por rol")
    void getUsersByRole_Success() {

        when(userRepository.findByRoleAndDeletedAtIsNull(UserRole.USER)).thenReturn(Arrays.asList(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);


        List<UserDTO> result = userService.getUsersByRole(UserRole.USER);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(UserRole.USER);
        verify(userRepository).findByRoleAndDeletedAtIsNull(UserRole.USER);
    }

    @Test
    @DisplayName("getUsersByRole - Debería retornar lista vacía si no hay usuarios con ese rol")
    void getUsersByRole_EmptyList() {

        when(userRepository.findByRoleAndDeletedAtIsNull(UserRole.ADMIN)).thenReturn(List.of());


        List<UserDTO> result = userService.getUsersByRole(UserRole.ADMIN);


        assertThat(result).isEmpty();
        verify(userRepository).findByRoleAndDeletedAtIsNull(UserRole.ADMIN);
    }

    // ==================== UPDATE USER TESTS ====================

    @Test
    @DisplayName("updateUser - Debería actualizar email exitosamente")
    void updateUser_Success() {

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("newemail@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);


        UserDTO result = userService.updateUser(1L, updateDTO);


        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(userMapper).updateUserFromDTO(updateDTO, testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("updateUser - Debería actualizar contraseña encriptada")
    void updateUser_WithPassword() {

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setPassword("newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);


        UserDTO result = userService.updateUser(1L, updateDTO);


        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("updateUser - Debería lanzar excepción con ID inexistente")
    void updateUser_NotFound() {

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("new@example.com");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.updateUser(999L, updateDTO))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser - Debería lanzar excepción con username duplicado")
    void updateUser_DuplicateUsername() {

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("existinguser");

        User anotherUser = new User();
        anotherUser.setUsername("existinguser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(anotherUser));


        assertThatThrownBy(() -> userService.updateUser(1L, updateDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("username");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser - Debería lanzar excepción con email duplicado")
    void updateUser_DuplicateEmail() {

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);


        assertThatThrownBy(() -> userService.updateUser(1L, updateDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser - Debería lanzar excepción con contraseña corta")
    void updateUser_PasswordTooShort() {

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setPassword("123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));


        assertThatThrownBy(() -> userService.updateUser(1L, updateDTO))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("contraseña")
                .hasMessageContaining("6 caracteres");

        verify(userRepository, never()).save(any());
    }

    // ==================== DELETE USER TESTS ====================

    @Test
    @DisplayName("deleteUser - Debería hacer soft delete")
    void deleteUser_Success() {

        when(userRepository.findByIdActive(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);


        userService.deleteUser(1L);


        verify(userRepository).findByIdActive(1L);
        verify(userRepository).save(testUser);
        assertThat(testUser.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteUser - Debería lanzar excepción con ID inexistente")
    void deleteUser_NotFound() {

        when(userRepository.findByIdActive(999L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("restoreUser - Debería restaurar usuario eliminado")
    void restoreUser_Success() {

        testUser.softDelete();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);


        UserDTO result = userService.restoreUser(1L);


        assertThat(result).isNotNull();
        assertThat(testUser.getDeletedAt()).isNull();
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("restoreUser - Debería lanzar excepción con ID inexistente")
    void restoreUser_NotFound() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.restoreUser(999L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("permanentlyDeleteUser - Debería eliminar permanentemente")
    void permanentlyDeleteUser_Success() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        userService.permanentlyDeleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("permanentlyDeleteUser - Debería lanzar excepción con ID inexistente")
    void permanentlyDeleteUser_NotFound() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.permanentlyDeleteUser(999L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }
}