package com.r2s.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Table(name = "users")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User implements Serializable {
    @Id
    @Column(columnDefinition = "UUID")
    UUID id;

    @Column(nullable = false, unique = true, length = 50)
    String username;

    @Column(nullable = false, length = 255)
    String password;

    @Column(nullable = false)
    Boolean enabled = true;

    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    Set<Role> roles;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}
