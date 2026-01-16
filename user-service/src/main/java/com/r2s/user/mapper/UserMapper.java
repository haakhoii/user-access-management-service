package com.r2s.user.mapper;

import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.user.entity.UserProfiles;

import java.util.List;
import java.util.UUID;
public class UserMapper {

    public static UserProfiles toEntity(UserCreatedRequest request, UUID userId) {
        return UserProfiles.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .avatarUrl(request.getAvatarUrl())
                .build();
    }

    public static UserResponse toResponse(
            UserProfiles profile,
            String username,
            List<String> roles
    ) {
        return UserResponse.builder()
                .username(username)
                .role(roles)
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .build();
    }
}
