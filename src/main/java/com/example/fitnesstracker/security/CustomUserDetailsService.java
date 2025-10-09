package com.example.fitnesstracker.security;

import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Servicio personalizado para cargar detalles de usuarios.
 *
 * Implementa UserDetailsService de Spring Security para integrar
 * nuestra entidad User con el sistema de autenticación de Spring.
 *
 * @author Fitness Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga un usuario por su username.
     *
     * Este método es llamado por Spring Security durante el proceso
     * de autenticación para obtener los detalles del usuario.
     *
     * @param username Nombre de usuario
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Usuario eliminado o inactivo: " + username);
        }

        // ✅ IMPORTANTE: Crear authority con el rol del usuario
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                user.getEnable(), // enabled
                true,              // accountNonExpired
                true,              // credentialsNonExpired
                true,              // accountNonLocked
                Collections.singletonList(authority) // ✅ Lista de authorities
        );
    }

    /**
     * Construye un objeto UserDetails a partir de nuestro User.
     *
     * @param user Usuario de nuestra base de datos
     * @return UserDetails para Spring Security
     */
    private UserDetails buildUserDetails(User user) {
        List<GrantedAuthority> authorities = getAuthorities(user);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getEnable())
                .build();
    }

    /**
     * Obtiene las autoridades (roles) del usuario.
     *
     * Convierte el rol de nuestra entidad al formato que espera Spring Security.
     * Los roles en Spring Security deben tener el prefijo "ROLE_".
     *
     * @param user Usuario
     * @return Lista de autoridades
     */
    private List<GrantedAuthority> getAuthorities(User user) {
        String roleName = "ROLE_" + user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }
}