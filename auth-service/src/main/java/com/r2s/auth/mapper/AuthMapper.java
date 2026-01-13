package com.r2s.auth.mapper;

import com.r2s.auth.entity.Auth;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.UserResponse;

public class AuthMapper {
    public static Auth toUser(RegisterRequest request) {
        return Auth.builder()
                .username(request.getUsername())
                .build();
    }

    public static UserResponse toUserResponse(Auth user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
