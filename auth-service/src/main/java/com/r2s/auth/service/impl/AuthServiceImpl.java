package com.r2s.auth.service.impl;

import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.auth.mapper.AuthMapper;
import com.r2s.auth.repository.AuthRepository;
import com.r2s.auth.repository.RoleRepository;
import com.r2s.auth.service.AuthService;
import com.r2s.auth.token.JwtToken;
import com.r2s.core.dto.request.IntrospectRequest;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthServiceImpl implements AuthService {
    AuthRepository authRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    JwtToken jwtToken;

    @Override
    public String register(RegisterRequest request) {
        if (authRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTS);
        }

        User user = AuthMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<Role> roles = new HashSet<>();

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        roles.add(defaultRole);
        if (request.getRole() != null && !request.getRole().isBlank()) {
            String roleRequest = request.getRole().trim().toUpperCase();
            if (!roleRequest.startsWith("ROLE_")) {
                roleRequest = "ROLE_" + roleRequest;
            }
            Role role = roleRepository.findByName(roleRequest)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            roles.add(role);
        }
        user.setRoles(roles);
        authRepository.save(user);

        log.info("User created: userId={}, roles={}",
                user.getId(),
                roles.stream().map(Role::getName).toList());

        return "User created successfully with userId = " + user.getId();
    }


    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating user with username: {}", request.getUsername());
        User user = authRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password for username={}", request.getUsername());
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        log.info("Login success for username: {}", user.getUsername());
        return jwtToken.generateToken(user);
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        try {
            Jwt jwt = jwtToken.verify(request.getToken());
            log.info("Token introspect success: sub={}", jwt.getSubject());

            return IntrospectResponse.builder()
                    .valid(true)
                    .sub(jwt.getSubject())
                    .username(jwt.getClaim("username"))
                    .exp(jwt.getExpiresAt())
                    .scope(jwt.getClaim("scope"))
                    .build();
        } catch (Exception e) {
            log.warn("Token introspect failed");
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
    }
}