package com.r2s.user_service.mapper;

import com.r2s.core_service.dto.response.UserResponse;
import com.r2s.user_service.entity.User;

public class UserMapper {
    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }
}
