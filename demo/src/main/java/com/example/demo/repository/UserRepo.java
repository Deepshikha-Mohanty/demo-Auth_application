package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {

    // Local Login
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    // OIDC Login (Gluu)
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}