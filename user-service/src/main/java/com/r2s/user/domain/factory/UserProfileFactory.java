package com.r2s.user.domain.factory;

import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.user.entity.UserProfiles;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class UserProfileFactory {

    public UserProfiles create(
            UserCreatedRequest request,
            UUID userId,
            String username,
            List<String> roles
    ) {
        return UserProfiles.builder()
                .userId(userId)
                .username(username)
                .roles(roles)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .avatarUrl(request.getAvatarUrl())
                .build();
    }

}


