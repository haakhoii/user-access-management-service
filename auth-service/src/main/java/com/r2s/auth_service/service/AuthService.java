package com.r2s.auth_service.service;

import com.r2s.auth_service.config.JwtToken;
import com.r2s.auth_service.entity.Auth;
import com.r2s.auth_service.mapper.AuthMapper;
import com.r2s.auth_service.repository.AuthRepository;
import com.r2s.core_service.dto.request.LoginRequest;
import com.r2s.core_service.dto.request.RegisterRequest;
import com.r2s.core_service.dto.response.TokenResponse;
import com.r2s.core_service.dto.response.AccountResponse;
import com.r2s.core_service.enums.Role;
import com.r2s.core_service.exception.AppException;
import com.r2s.core_service.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    AuthRepository authRepository;
    PasswordEncoder passwordEncoder;
    JwtToken jwtToken;

    public AccountResponse register(RegisterRequest request) {
        if (authRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTS);
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }
        Auth auth = AuthMapper.toAuth(request);
        auth.setPassword(passwordEncoder.encode(request.getPassword()));
        auth.setRole(Role.USER);
        auth = authRepository.save(auth);

        return AuthMapper.toAccountResponse(auth);
    }

    public TokenResponse login(LoginRequest request) {
        Auth auth = authRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getPassword(), auth.getPassword()))
            throw new AppException(ErrorCode.PASSWORD_INVALID);

        String token = jwtToken.generateToken(auth);
        Instant expiry = jwtToken.generateExpiry();

        return TokenResponse.builder()
                .token(token)
                .expiry(expiry)
                .build();
    }
}
