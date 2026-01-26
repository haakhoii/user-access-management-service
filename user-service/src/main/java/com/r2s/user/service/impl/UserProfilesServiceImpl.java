package com.r2s.user.service.impl;

import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserProfileResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import com.r2s.user.entity.UserProfiles;
import com.r2s.user.mapper.UserProfilesMapper;
import com.r2s.user.repository.UserProfileRepository;
import com.r2s.user.service.UserProfilesService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserProfilesServiceImpl implements UserProfilesService {
    UserProfileRepository userProfileRepository;

    private JwtAuthenticationToken jwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwt)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return jwt;
    }

    private UUID getUserId() {
        return UUID.fromString(jwt().getName());
    }

    private String getUsername() {
        return jwt().getToken().getClaimAsString("username");
    }

    private List<String> getRoles() {
        String scope = jwt().getToken().getClaimAsString("scope");
        return List.of(scope.split(" "));
    }

    @Override
    public UserProfileResponse create(UserCreatedRequest request) {
        UUID userId = getUserId();

        userProfileRepository.findByUserId(userId)
                .ifPresent(u -> {
                    throw new AppException(ErrorCode.USER_EXISTS);
                });

        UserProfiles profile = UserProfilesMapper.toUser(
                request,
                userId,
                getUsername(),
                getRoles()
        );

        userProfileRepository.save(profile);

        UserProfileResponse response = UserProfilesMapper.toUserResponse(profile);

        log.info("User profile created successfully: {}", response);
        return response;
    }

    @Override
    public UserProfileResponse getMe() {
        UserProfiles profile = userProfileRepository.findByUserId(getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserProfileResponse response = UserProfilesMapper.toUserResponse(profile);
        log.info("Get my profile: {}", response);

        return response;
    }

    @Override
    public PageResponse<UserProfileResponse> getList(int page, int size) {

        if (page <= 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<UserProfiles> pageData = userProfileRepository.findAll(pageable);

        List<UserProfileResponse> data = pageData.getContent()
                .stream()
                .map(UserProfilesMapper::toUserResponse)
                .toList();

        log.info("Fetched user list: page={}, size={}, total={}",
                page,
                size,
                pageData.getTotalElements()
        );

        return PageResponse.<UserProfileResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(data)
                .build();
    }

    @Override
    public UserProfileResponse update(UserUpdatedRequest request) {

        UserProfiles profile = userProfileRepository.findByUserId(getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            profile.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        userProfileRepository.save(profile);

        UserProfileResponse response = UserProfilesMapper.toUserResponse(profile);
        log.info("User profile updated successfully: {}", response);

        return response;
    }

    @Override
    public String delete(UUID id) {

        UserProfiles profile = userProfileRepository.findByUserId(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userProfileRepository.delete(profile);

        log.info("User profile deleted successfully, userId={}", id);
        return "User profile deleted successfully";
    }
}
