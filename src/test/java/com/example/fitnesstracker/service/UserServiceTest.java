package com.example.fitnesstracker.service;

import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.exception.InvalidUserDataException;
import com.example.fitnesstracker.exception.UserAlreadyExistsException;
import com.example.fitnesstracker.exception.UserNotFoundException;
import com.example.fitnesstracker.mapper.UserMapper;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private UserRegisterDTO testRegisterDTO;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setExternalId("550e8400-e29b-41d4-a716-446655440000");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setEnable(true);
        testUser.setRole(UserRole.MEMBER);

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setExternalId("550e8400-e29b-41d4-a716-446655440000");
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setEnable(true);
        testUserDTO.setRole(UserRole.MEMBER);

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
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserRegisterDTO.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.registerUser(testRegisterDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con username vacío")
    void registerUser_EmptyUsername() {
        // Arrange
        testRegisterDTO.setUsername("");

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("username")
                .hasMessageContaining("obligatorio");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con email vacío")
    void registerUser_EmptyEmail() {
        // Arrange
        testRegisterDTO.setEmail("");

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("email")
                .hasMessageContaining("obligatorio");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con contraseña corta")
    void registerUser_PasswordTooShort() {
        // Arrange
        testRegisterDTO.setPassword("123");

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("contraseña")
                .hasMessageContaining("6 caracteres");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con username duplicado")
    void registerUser_DuplicateUsername() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("newuser");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser - Debería lanzar excepción con email duplicado")
    void registerUser_DuplicateEmail() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(testRegisterDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("new@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser - Debería encriptar la contraseña")
    void registerUser_ShouldEncryptPassword() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserRegisterDTO.class))).thenReturn(testUser);
        when(passwordEncoder.encode("password123")).thenReturn("encryptedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

        // Act
        userService.registerUser(testRegisterDTO);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(passwordEncoder).encode("password123");
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("login - Debería hacer login exitosamente")
    void login_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.login("testuser", "password123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    @DisplayName("login - Debería lanzar excepción con usuario inexistente")
    void login_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.login("noexiste", "password123"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("noexiste");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("login - Debería lanzar excepción con contraseña incorrecta")
    void login_WrongPassword() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.login("testuser", "wrongpassword"))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Credenciales inválidas");
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

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDTO::getUsername)
                .containsExactly("testuser", "user2");
        verify(userRepository).findAllActive();
    }

    @Test
    @DisplayName("getUserById - Debería retornar usuario activo por ID")
    void getUserById_Success() {
        // Arrange
        when(userRepository.findByIdActive(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.getUserById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findByIdActive(1L);
    }

    @Test
    @DisplayName("getUserById - Debería lanzar excepción con ID inexistente o eliminado")
    void getUserById_NotFound() {
        // Arrange
        when(userRepository.findByIdActive(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository).findByIdActive(999L);
    }

    @Test
    @DisplayName("getUserByExternalId - Debería retornar usuario por UUID")
    void getUserByExternalId_Success() {
        // Arrange
        String externalId = "550e8400-e29b-41d4-a716-446655440000";
        when(userRepository.findByExternalId(externalId)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.getUserByExternalId(externalId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getExternalId()).isEqualTo(externalId);
        verify(userRepository).findByExternalId(externalId);
    }

    @Test
    @DisplayName("getUsersByRole - Debería filtrar usuarios por rol")
    void getUsersByRole_Success() {
        // Arrange
        when(userRepository.findByRole(UserRole.MEMBER)).thenReturn(Arrays.asList(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        List<UserDTO> result = userService.getUsersByRole(UserRole.MEMBER);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(UserRole.MEMBER);
        verify(userRepository).findByRole(UserRole.MEMBER);
    }

    // ==================== UPDATE USER TESTS ====================

    @Test
    @DisplayName("updateUser - Debería actualizar usuario exitosamente")
    void updateUser_Success() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("newemail@example.com");
        updateDTO.setRole(UserRole.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.updateUser(1L, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("updateUser - Debería lanzar excepción con ID inexistente")
    void updateUser_NotFound() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("new@example.com");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(999L, updateDTO))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser - Debería lanzar excepción con email duplicado")
    void updateUser_DuplicateEmail() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(1L, updateDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("existing@example.com");

        verify(userRepository, never()).save(any());
    }

    // ==================== DELETE USER TESTS ====================

    @Test
    @DisplayName("deleteUser - Debería hacer soft delete del usuario")
    void deleteUser_Success() {
        // Arrange
        when(userRepository.findByIdActive(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).findByIdActive(1L);
        verify(userRepository).save(testUser);
        // Verificar que se llamó al método softDelete() (deletedAt se setea)
    }

    @Test
    @DisplayName("deleteUser - Debería lanzar excepción con ID inexistente")
    void deleteUser_NotFound() {
        // Arrange
        when(userRepository.findByIdActive(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("restoreUser - Debería restaurar usuario eliminado")
    void restoreUser_Success() {
        // Arrange
        testUser.softDelete(); // Marcar como eliminado
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.restoreUser(1L);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("restoreUser - Debería lanzar excepción si usuario no está eliminado")
    void restoreUser_NotDeleted() {
        // Arrange - Usuario activo (no eliminado)
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.restoreUser(1L))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("no está eliminado");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("permanentlyDeleteUser - Debería eliminar permanentemente")
    void permanentlyDeleteUser_Success() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.permanentlyDeleteUser(1L);

        // Assert
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("permanentlyDeleteUser - Debería lanzar excepción con ID inexistente")
    void permanentlyDeleteUser_NotFound() {
        // Arrange
        when(userRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.permanentlyDeleteUser(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository, never()).deleteById(anyLong());
    }
}