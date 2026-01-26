package com.r2s.user.mapper;

import com.r2s.core.dto.response.UserProfileResponse;
import com.r2s.user.entity.UserProfiles;


public class UserProfilesMapper {
    public static UserProfileResponse toUserResponse(UserProfiles profile) {
        return UserProfileResponse.builder()
                .username(profile.getUsername())
                .role(profile.getRoles())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .build();
    }
}
