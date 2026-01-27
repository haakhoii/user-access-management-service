package com.r2s.auth.mapper;

import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.dto.response.UserResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.stream.Collectors;

public class UserMapper {
    public static IntrospectResponse toIntrospectResponses(Jwt jwt) {
        return IntrospectResponse.builder()
                .valid(true)
                .sub(jwt.getSubject())
                .username(jwt.getClaim("username"))
                .exp(jwt.getExpiresAt())
                .scope(jwt.getClaim("roles"))
                .build();
    }

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
                )
                .build();
    }
}
