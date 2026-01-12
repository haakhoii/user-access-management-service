package com.r2s.auth.repository;

import com.r2s.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
}
