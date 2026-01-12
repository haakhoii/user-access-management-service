package com.r2s.core.mapper;

import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.core.entity.User;

public class UserMapper {
    public static User toUser(RegisterRequest request) {
        return User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .build();
    }

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}
