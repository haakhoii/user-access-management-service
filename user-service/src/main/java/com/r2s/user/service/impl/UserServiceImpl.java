package com.r2s.user.service.impl;

import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.user.entity.UserProfiles;
import com.r2s.user.mapper.UserMapper;
import com.r2s.user.repository.UserRepository;
import com.r2s.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    /* ================= JWT HELPERS ================= */

    private JwtAuthenticationToken jwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwt)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return jwt;
    }

    private UUID getUserId() {
        return UUID.fromString(jwt().getName()); // sub
    }

    private String getUsername() {
        return jwt().getToken().getClaimAsString("username");
    }

    private List<String> getRoles() {
        String scope = jwt().getToken().getClaimAsString("scope");
        return List.of(scope.split(" "));
    }

    /* ================= BUSINESS ================= */

    @Override
    public UserResponse create(UserCreatedRequest request) {

        UUID userId = getUserId();

        userRepository.findByUserId(userId)
                .ifPresent(u -> {
                    throw new AppException(ErrorCode.USER_EXISTS);
                });

        UserProfiles profile = UserMapper.toEntity(request, userId);
        userRepository.save(profile);

        return UserMapper.toResponse(
                profile,
                getUsername(),
                getRoles()
        );
    }

    @Override
    public UserResponse getMe() {

        UserProfiles profile = userRepository.findByUserId(getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return UserMapper.toResponse(
                profile,
                getUsername(),
                getRoles()
        );
    }

    @Override
    public PageResponse<UserResponse> getList(int page, int size) {

        if (page <= 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by("createdAt").descending()
        );

        Page<UserProfiles> pageData = userRepository.findAll(pageable);

        List<UserResponse> data = pageData.getContent()
                .stream()
                .map(p -> UserMapper.toResponse(
                        p,
                        getUsername(),
                        getRoles()
                ))
                .toList();

        return PageResponse.<UserResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(data)
                .build();
    }

    @Override
    public UserResponse update(UserUpdatedRequest request) {

        UserProfiles profile = userRepository.findByUserId(getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getEmail() != null) profile.setEmail(request.getEmail());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());

        userRepository.save(profile);

        return UserMapper.toResponse(
                profile,
                getUsername(),
                getRoles()
        );
    }

    @Override
    public String delete(UUID id) {

        UserProfiles profile = userRepository.findById(id.toString())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(profile);

        return "User profile deleted successfully with id = " + id;
    }
}