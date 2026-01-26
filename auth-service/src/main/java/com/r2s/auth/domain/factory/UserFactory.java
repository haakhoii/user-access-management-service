package com.r2s.auth.domain.factory;

import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.core.dto.request.RegisterRequest;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserFactory {

    public User create(RegisterRequest request,
                       Set<Role> roles,
                       String encodedPassword) {

        return User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .roles(roles)
                .enabled(true)
                .build();
    }
}

