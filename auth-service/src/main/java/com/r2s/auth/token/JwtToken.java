package com.r2s.auth.token;

import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtToken {
    AuthResponse generateToken(User user);

    Jwt verify(String token);
}
