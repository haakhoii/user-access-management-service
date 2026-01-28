package com.r2s.auth.service.impl;

import com.r2s.auth.domain.rateLimit.RateLimitRedisKey;
import com.r2s.auth.domain.validation.authentication.AuthenticationValidation;
import com.r2s.auth.entity.User;
import com.r2s.auth.mapper.UserMapper;
import com.r2s.auth.service.AuthenticationService;
import com.r2s.auth.service.RateLimitService;
import com.r2s.auth.token.JwtToken;
import com.r2s.core.constants.RateLimitType;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    JwtToken jwtToken;
    AuthenticationValidation authenticationValidation;
    RateLimitService rateLimitService;

    @Override
    public TokenResponse login(LoginRequest request) {

        String username = request.getUsername().toLowerCase();
        String baseKey = "rate_limit:POST:/auth/login:" + username;

        try {
            User user = authenticationValidation.validateLogin(request);

            rateLimitService.onSuccess(baseKey);
            return jwtToken.generateToken(user);

        } catch (Exception ex) {

            rateLimitService.onFailure(
                    baseKey,
                    RateLimitType.ATTEMPTS,
                    Duration.ofMinutes(RateLimitType.TIME_TO_LIVE)
            );
            throw ex;
        }
    }


    @Override
    public IntrospectResponse introspect() {
        Jwt jwt = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        log.info("Token introspect success userId: {}", jwt.getSubject());
        return UserMapper.toIntrospectResponse(jwt);
    }

}
