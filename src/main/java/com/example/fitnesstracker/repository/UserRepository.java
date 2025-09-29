package com.example.fitnesstracker.repository;

import com.example.fitnesstracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Podés definir métodos custom si querés, por ahora con JpaRepository alcanza
}
