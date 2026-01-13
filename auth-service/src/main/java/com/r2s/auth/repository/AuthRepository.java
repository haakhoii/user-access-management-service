package com.r2s.auth.repository;

import com.r2s.auth.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, String> {
    Optional<Auth> findByUsername(String username);
}
