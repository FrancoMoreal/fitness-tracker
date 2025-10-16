package com.example.fitnesstracker.model;

import com.example.fitnesstracker.model.common.BaseEntity;
import com.example.fitnesstracker.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"member", "trainer"}, callSuper = false)
@ToString(exclude = {"member", "trainer"})
public class User extends BaseEntity implements UserDetails {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private Boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // ========== Relaciones Opcionales ==========
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Member member;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Trainer trainer;

    // ========== UserDetails Implementation ==========
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
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
        return this.member != null && this.member.getIsActive();
    }

    public boolean isTrainer() {
        return this.trainer != null && this.trainer.getIsActive();
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
    }
}