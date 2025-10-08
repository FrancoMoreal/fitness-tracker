package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Busca usuario por username (solo activos)
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * Busca usuario por email (solo activos)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Verifica si existe un email (solo activos)
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Busca usuario por ID (solo activos)
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdActive(@Param("id") Long id);

    /**
     * Busca usuario por external ID (UUID)
     */
    @Query("SELECT u FROM User u WHERE u.externalId = :externalId AND u.deletedAt IS NULL")
    Optional<User> findByExternalId(@Param("externalId") String externalId);

    // ==================== QUERIES POR ROL ====================

    /**
     * Busca usuarios por rol (solo activos)
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    List<User> findByRole(@Param("role") UserRole role);

    // ==================== QUERIES PARA LISTAR ====================

    /**
     * Obtiene todos los usuarios activos (no eliminados)
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAllActive();

    /**
     * Obtiene todos los usuarios (incluidos eliminados) - Para administración
     */
    @Query("SELECT u FROM User u")
    List<User> findAllIncludingDeleted();

    /**
     * Obtiene solo usuarios eliminados - Para recuperación
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NOT NULL")
    List<User> findAllDeleted();

    // ==================== OPERACIONES ESPECIALES ====================

    /**
     * Cuenta usuarios activos
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL")
    long countActive();

    /**
     * Verifica si un usuario está activo
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    boolean isActive(@Param("id") Long id);
}