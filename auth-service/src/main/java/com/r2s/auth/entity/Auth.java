package com.r2s.auth.entity;

import com.r2s.core.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Table(name = "tbl_auth")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Auth implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String username;

    String password;

    @Enumerated(EnumType.STRING)
    Role role;

}
