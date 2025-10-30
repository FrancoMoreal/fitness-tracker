
package com.example.fitnesstracker.config;

import com.example.fitnesstracker.enums.UserRole;
import com.example.fitnesstracker.model.User;
import com.example.fitnesstracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminUsername = "admin1";
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            User admin = User.builder()
                    .username(adminUsername)
                    .email("admin1@example.com")
                    .password(passwordEncoder.encode("admin123")) // cambiar después
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Usuario admin creado: {}", adminUsername);
        } else {
            log.info("Usuario admin ya existe");
        }
    }
}
