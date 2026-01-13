package com.r2s.auth.service.impl;

import com.r2s.auth.mapper.AuthMapper;
import com.r2s.auth.repository.AuthRepository;
import com.r2s.auth.service.AuthService;
import com.r2s.auth.token.JwtToken;
import com.r2s.core.dto.request.IntrospectRequest;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.auth.entity.Auth;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.enums.Role;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {
    AuthRepository authRepository;
    PasswordEncoder passwordEncoder;
    JwtToken jwtToken;

    @Override
    public String register(RegisterRequest request) {
        if (authRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTS);
        }

        Auth auth = AuthMapper.toUser(request);
        auth.setPassword(passwordEncoder.encode(request.getPassword()));
        auth.setRole(Role.USER);
        authRepository.save(auth);

        return "User created successfully with userId = " + auth.getId();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Auth auth = authRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getPassword(), auth.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        return jwtToken.generateToken(auth);
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        Jwt jwt = jwtToken.verify(request.getToken());

        return IntrospectResponse.builder()
                .valid(true)
                .sub(jwt.getSubject())
                .username(jwt.getClaim("username"))
                .exp(jwt.getExpiresAt())
                .scope(jwt.getClaim("scope"))
                .build();
    }
}