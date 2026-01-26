package com.r2s.user.repository;

import com.r2s.user.entity.UserProfiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfiles, UUID> {
    Optional<UserProfiles> findByUserId(UUID userId);
}
