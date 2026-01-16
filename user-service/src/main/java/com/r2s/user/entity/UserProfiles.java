package com.r2s.user.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table(name = "user_profiles")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfiles implements Serializable {
    @Id
    @Column(columnDefinition = "UUID")
    UUID id;

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @Column(name = "full_name", length = 100)
    String fullName;

    @Column(unique = true, length = 100)
    String email;

    @Column(length = 20)
    String phone;

    @Column(columnDefinition = "TEXT")
    String address;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    String avatarUrl;

    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
