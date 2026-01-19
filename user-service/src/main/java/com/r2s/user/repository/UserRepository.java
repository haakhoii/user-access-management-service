package com.r2s.user.repository;

import com.r2s.user.entity.UserProfiles;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserProfiles, String> {
    Optional<UserProfiles> findByUserId(UUID userId);

    @Query("""
        SELECT u FROM UserProfiles u
        WHERE (:cursor IS NULL OR u.createdAt < :cursor)
        ORDER BY u.createdAt DESC
    """)
    List<UserProfiles> findByCursor(
            @Param("cursor") LocalDateTime cursor,
            Pageable pageable
    );
}
