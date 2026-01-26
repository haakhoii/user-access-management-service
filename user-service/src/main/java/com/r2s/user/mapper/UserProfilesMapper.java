package com.r2s.user.mapper;

import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.response.UserProfileResponse;
import com.r2s.user.entity.UserProfiles;

import java.util.List;
import java.util.UUID;

public class UserProfilesMapper {

    public static UserProfiles toUser(
            UserCreatedRequest request,
            UUID userId,
            String username,
            List<String> roles
    ) {
        return UserProfiles.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .username(username)
                .roles(String.join(",", roles))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .avatarUrl(request.getAvatarUrl())
                .build();
    }

    public static UserProfileResponse toUserResponse(UserProfiles profile) {
        return UserProfileResponse.builder()
                .username(profile.getUsername())
                .role(profile.getRoles() != null
                        ? List.of(profile.getRoles().split(","))
                        : List.of()
                )
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .build();
    }
}
