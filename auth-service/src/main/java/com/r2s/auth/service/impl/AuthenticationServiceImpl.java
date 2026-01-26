package com.r2s.auth.service.impl;

import com.r2s.auth.domain.validation.authentication.AuthenticationValidation;
import com.r2s.auth.entity.User;
import com.r2s.auth.mapper.UserMapper;
import com.r2s.auth.service.AuthenticationService;
import com.r2s.auth.token.JwtToken;
import com.r2s.core.dto.request.IntrospectRequest;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.dto.response.TokenResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    JwtToken jwtToken;
    AuthenticationValidation authenticationValidation;

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = authenticationValidation.validateLogin(request);
        log.info("Login success for username: {}", user.getUsername());

        return jwtToken.generateToken(user);
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        try {
            Jwt jwt = jwtToken.verify(request.getToken());
            log.info("Token introspect success: userId: {}", jwt.getSubject());

            return UserMapper.toIntrospectResponses(jwt);
        } catch (JwtException e) {
            log.warn("Token introspect failed: {}", e.getMessage());
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
    }
}
