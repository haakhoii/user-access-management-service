package com.r2s.auth.mapper;

import com.r2s.auth.entity.User;
import com.r2s.core.dto.request.RegisterRequest;

import java.util.UUID;

public class AuthMapper {
    public static User toUser(RegisterRequest request) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .enabled(true)
                .build();
    }

//    public static UserResponse toUserResponse(User user) {
//        return UserResponse.builder()
//                .username(user.getUsername())
//                .role(user.getRoles()
//                        .stream()
//                        .map(Role::getName)
//                        .toList())
//                .build();
//    }
}
