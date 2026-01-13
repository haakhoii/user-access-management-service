package com.r2s.user.service.impl;

import com.nimbusds.jwt.SignedJWT;
import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import com.r2s.user.entity.User;
import com.r2s.user.mapper.UserMapper;
import com.r2s.user.repository.UserRepository;
import com.r2s.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;

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
    public UserResponse create(UserCreatedRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String userId = getUserId();
        String username = jwt.getToken().getClaimAsString("username");
        String role = jwt.getToken().getClaimAsString("scope");

        User user = UserMapper.toUser(request);
        user.setUserId(userId);
        user.setUsername(username);
        user.setRole(role);
        userRepository.save(user);

        return UserMapper.toUserResponse(user);
    }

    @Override
    public PageResponse<UserResponse> getList(int page, int size) {
        if (page <= 0 || size <= 0)
            throw new AppException(ErrorCode.INVALID_REQUEST);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> userPage = userRepository.findAll(pageable);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username;
        String role;

        if (authentication instanceof JwtAuthenticationToken jwt) {
            username = jwt.getToken().getClaimAsString("username");
            role = jwt.getToken().getClaimAsString("scope");
        } else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

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
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserMapper.toUserResponse(user);
    }

    @Override
    public UserResponse update(UserUpdatedRequest request) {
        String userId = getUserId();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        userRepository.save(user);

        return UserMapper.toUserResponse(user);
    }

    @Override
    public String delete(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
        return "User deleted successfully with userId = " + id;
    }
}
