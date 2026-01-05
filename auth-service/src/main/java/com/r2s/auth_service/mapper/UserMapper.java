package com.r2s.auth_service.mapper;

import com.r2s.auth_service.entity.User;
import com.r2s.core_service.dto.request.UserCreationRequest;
import com.r2s.core_service.dto.response.UserResponse;

public class UserMapper {
    public static User toUser(UserCreationRequest request) {
        return User.builder()
                .username(request.getUsername())
                .build();
    }

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
