package com.example.fitnesstracker.mapper;

import com.example.fitnesstracker.dto.request.UserRegisterDTO;
import com.example.fitnesstracker.dto.request.UserUpdateDTO;
import com.example.fitnesstracker.dto.response.UserDTO;
import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("UserMapper Unit Tests")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setEnable(true);
        testUser.setRole(UserRole.USER);
    }

    @Test
    @DisplayName("toDto - Debería convertir User a UserDTO correctamente")
    void toDto_Success() {
        // Act
        UserDTO result = userMapper.toDto(testUser);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getEnable()).isTrue();
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("toDto - No debería incluir password")
    void toDto_ShouldNotIncludePassword() {
        // Act
        UserDTO result = userMapper.toDto(testUser);

        // Assert
        // UserDTO no tiene campo password, verificamos que no lance error
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("toDto - Debería manejar null correctamente")
    void toDto_NullInput() {
        // Act
        UserDTO result = userMapper.toDto(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toEntity - Debería convertir UserRegisterDTO a User")
    void toEntity_Success() {
        // Arrange
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setEmail("new@example.com");
        registerDTO.setPassword("password123");

        // Act
        User result = userMapper.toEntity(registerDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getId()).isNull(); // No debe setear el ID
    }

    @Test
    @DisplayName("toEntity - Debería manejar null correctamente")
    void toEntity_NullInput() {
        // Act
        User result = userMapper.toEntity(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("updateUserFromDTO - Debería actualizar campos no null")
    void updateUserFromDTO_Success() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setRole(UserRole.ADMIN);
        updateDTO.setEnable(false);

        // Act
        userMapper.updateUserFromDTO(updateDTO, testUser);

        // Assert
        assertThat(testUser.getUsername()).isEqualTo("updateduser");
        assertThat(testUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(testUser.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(testUser.getEnable()).isFalse();
        assertThat(testUser.getId()).isEqualTo(1L); // ID no debe cambiar
    }

    @Test
    @DisplayName("updateUserFromDTO - Debería ignorar campos null")
    void updateUserFromDTO_IgnoreNullFields() {
        // Arrange
        String originalUsername = testUser.getUsername();
        String originalEmail = testUser.getEmail();
        UserRole originalRole = testUser.getRole();

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEnable(false); // Solo actualizar enabled

        // Act
        userMapper.updateUserFromDTO(updateDTO, testUser);

        // Assert
        assertThat(testUser.getUsername()).isEqualTo(originalUsername);
        assertThat(testUser.getEmail()).isEqualTo(originalEmail);
        assertThat(testUser.getRole()).isEqualTo(originalRole);
        assertThat(testUser.getEnable()).isFalse(); // Solo este cambió
    }

    @Test
    @DisplayName("updateUserFromDTO - Debería manejar null DTO")
    void updateUserFromDTO_NullDTO() {
        // Arrange
        String originalUsername = testUser.getUsername();

        // Act
        userMapper.updateUserFromDTO(null, testUser);

        // Assert
        assertThat(testUser.getUsername()).isEqualTo(originalUsername);
        // El usuario no debe cambiar
    }

    @Test
    @DisplayName("updateUserFromDTO - No debería actualizar password")
    void updateUserFromDTO_ShouldNotUpdatePassword() {
        // Arrange
        String originalPassword = testUser.getPassword();

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setPassword("newPassword"); // Este campo debe ser ignorado

        // Act
        userMapper.updateUserFromDTO(updateDTO, testUser);

        // Assert
        assertThat(testUser.getPassword()).isEqualTo(originalPassword);
        // El password se maneja en el Service, no en el Mapper
    }
}