package com.r2s.auth_service.service;

import com.r2s.auth_service.config.JwtToken;
import com.r2s.auth_service.entity.User;
import com.r2s.auth_service.mapper.UserMapper;
import com.r2s.auth_service.repository.UserRepository;
import com.r2s.core_service.dto.request.LoginRequest;
import com.r2s.core_service.dto.request.UserCreationRequest;
import com.r2s.core_service.dto.response.AuthResponse;
import com.r2s.core_service.dto.response.UserResponse;
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
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtToken jwtToken;

    public UserResponse register(UserCreationRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }
        User user = UserMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user = userRepository.save(user);

        return UserMapper.toUserResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new AppException(ErrorCode.PASSWORD_INVALID);

        String token = jwtToken.generateToken(user);
        Instant expiry = jwtToken.generateExpiry();

        return AuthResponse.builder()
                .token(token)
                .expiry(expiry)
                .build();
    }
}
