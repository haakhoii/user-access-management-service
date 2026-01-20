package com.r2s.user.repository;

import com.r2s.user.entity.UserProfiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserProfiles, String> {
    Optional<UserProfiles> findByUserId(UUID userId);

}
