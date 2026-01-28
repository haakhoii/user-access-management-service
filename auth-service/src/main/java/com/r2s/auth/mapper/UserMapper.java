package com.r2s.auth.mapper;

import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.dto.response.UserResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    public static IntrospectResponse toIntrospectResponse(Jwt jwt) {
        return IntrospectResponse.builder()
                .valid(true)
                .userId(jwt.getSubject())
                .username(jwt.getClaimAsString("username"))
                .roles(jwt.getClaim("roles") instanceof List
                        ? jwt.getClaim("roles")
                        : List.of())
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
