package com.r2s.core.dto.response;

import lombok.*;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    UUID id;
    String username;
    Set<String> roles;
}