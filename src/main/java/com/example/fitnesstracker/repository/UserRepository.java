package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Búsquedas básicas
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    Optional<User> findByExternalId(String externalId);
    Optional<User> findByExternalIdAndDeletedAtIsNull(String externalId);


    // obtiene por id solo si no está eliminado
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdActive(@Param("id") Long id);
    // Búsquedas por rol
    List<User> findByRole(UserRole role);
    List<User> findByRoleAndDeletedAtIsNull(UserRole role);

    // Búsquedas por estado
    List<User> findByEnabled(Boolean enabled);
    List<User> findByEnabledAndDeletedAtIsNull(Boolean enabled);

    // Búsquedas complejas
    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.deletedAt IS NULL AND u.role = :role ORDER BY u.createdAt DESC")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    List<User> findAllActive();

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NOT NULL ORDER BY u.deletedAt DESC")
    List<User> findAllDeleted();

    // Existencias
    boolean existsByUsername(String username);
    boolean existsByUsernameAndDeletedAtIsNull(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndDeletedAtIsNull(String email);

    // Contar usuarios
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true AND u.deletedAt IS NULL")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    long countByRole(@Param("role") UserRole role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL")
    long countAllUsers();

    // Búsqueda case-insensitive
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL")
    Optional<User> findByEmailIgnoreCaseAndDeletedAtIsNull(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username) AND u.deletedAt IS NULL")
    Optional<User> findByUsernameIgnoreCaseAndDeletedAtIsNull(@Param("username") String username);
}
