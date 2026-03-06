package com.r2s.auth.domain.factory;

import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.core.dto.request.RegisterRequest;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserFactory {

    public User create(RegisterRequest request,
                       Role role,
                       String encodedPassword) {

        return User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .role(role)
                .enabled(true)
                .build();
    }
}

