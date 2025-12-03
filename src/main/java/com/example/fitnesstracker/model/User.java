package com.example.fitnesstracker.model;

import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.enums.UserType;
import com.example.fitnesstracker.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_type", columnList = "user_type"),
        @Index(name = "idx_user_enabled", columnList = "enabled")
})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(exclude = {"member", "trainer"}, callSuper = false)
@ToString(exclude = {"member", "trainer"})
public class User extends BaseEntity implements UserDetails {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType = UserType.NONE;

    // ========== Relaciones Opcionales (Lazy) ==========
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Member member;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Trainer trainer;

    // ========== UserDetails Implementation ==========
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled && this.isActive();
    }

    // ========== Helper Methods ==========
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isRegularUser() {
        return this.role == UserRole.USER;
    }

    public boolean isMember() {
        return this.userType == UserType.MEMBER && this.member != null;
    }

    public boolean isTrainer() {
        return this.userType == UserType.TRAINER && this.trainer != null;
    }

    public boolean hasProfile() {
        return this.userType != UserType.NONE;
    }

    /**
     * Obtiene el nombre completo del perfil asociado
     */
    public String getFullName() {
        if (isMember() && member != null) {
            return member.getFullName();
        }
        if (isTrainer() && trainer != null) {
            return trainer.getFullName();
        }
        return username;
    }

    @PrePersist
    public void prePersist() {
        super.onCreate();
        if (this.enabled == null) {
            this.enabled = true;
        }
        if (this.role == null) {
            this.role = UserRole.USER;
        }
        if (this.userType == null) {
            this.userType = UserType.NONE;
        }
    }
}
