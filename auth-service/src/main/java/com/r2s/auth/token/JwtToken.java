package com.r2s.auth.token;

import com.r2s.core.dto.response.TokenResponse;
import com.r2s.auth.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtToken {
    TokenResponse generateToken(User user);

    Jwt verify(String token);
}