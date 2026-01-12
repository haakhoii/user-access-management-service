package com.r2s.auth.service.impl;

import com.r2s.auth.repository.AuthRepository;
import com.r2s.auth.service.AuthService;
import com.r2s.auth.token.JwtToken;
import com.r2s.core.dto.request.IntrospectRequest;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.request.UpdateUserRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.core.entity.User;
import com.r2s.core.enums.Role;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import com.r2s.core.mapper.UserMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {
    AuthRepository authRepository;
    PasswordEncoder passwordEncoder;
    JwtToken jwtToken;

    @Override
    public UserResponse register(RegisterRequest request) {
        if (authRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTS);
        }

        User user = UserMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        authRepository.save(user);

        return UserMapper.toUserResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = authRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        return jwtToken.generateToken(user);
    }

    private String getUserId() {
        if (SecurityContextHolder.getContext() == null
                || SecurityContextHolder.getContext().getAuthentication() == null
                || SecurityContextHolder.getContext().getAuthentication().getName() == null
        ) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public UserResponse update(UpdateUserRequest request) {
        String userId = getUserId();
        User user = authRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        authRepository.save(user);

        return UserMapper.toUserResponse(user);
    }

    @Override
    public String delete() {
        String userId = getUserId();
        User user = authRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        authRepository.delete(user);
        return "User deleted successfully with userId = " + userId;
    }

    @Override
    public PageResponse<UserResponse> getList(int page, int size) {
        if (page <= 0 || size<= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> userPage = authRepository.findAll(pageable);

        List<UserResponse> data = userPage.getContent().stream()
                .map(UserMapper::toUserResponse)
                .toList();

        return PageResponse.<UserResponse>builder()
                .currentPage(page)
                .totalPages(userPage.getTotalPages())
                .pageSize(size)
                .totalElements(userPage.getTotalElements())
                .data(data)
                .build();
    }

    @Override
    public UserResponse getMe() {
        String userId = getUserId();
        User user = authRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserMapper.toUserResponse(user);
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
