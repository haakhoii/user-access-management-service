package com.r2s.user.mapper;

import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.user.entity.User;

public class UserMapper {
    public static User toUser(UserCreatedRequest request) {
        return User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .build();
    }

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .role(user.getRole())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }
}
