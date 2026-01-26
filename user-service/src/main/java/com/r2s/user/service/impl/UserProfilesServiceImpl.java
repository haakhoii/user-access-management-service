package com.r2s.user.service.impl;

import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserProfileResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import com.r2s.user.domain.factory.UserProfileFactory;
import com.r2s.user.domain.helper.SecurityContextHelper;
import com.r2s.user.domain.validation.UserProfileValidation;
import com.r2s.user.entity.UserProfiles;
import com.r2s.user.mapper.UserProfilesMapper;
import com.r2s.user.repository.UserProfileRepository;
import com.r2s.user.service.UserProfilesService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class UserProfilesServiceImpl implements UserProfilesService {
    UserProfileRepository userProfileRepository;
    SecurityContextHelper securityContextHelper;
    UserProfileFactory userProfileFactory;
    UserProfileValidation userProfileValidation;

    @Override
    public UserProfileResponse create(UserCreatedRequest request) {
        UUID userId = securityContextHelper.getCurrentUserId();
        userProfileValidation.validateCreate(userId);

        UserProfiles profile = userProfileFactory.create(
                request,
                userId,
                securityContextHelper.getCurrentUsername(),
                securityContextHelper.getCurrentRoles()
        );
        userProfileRepository.save(profile);
        log.info("User profile created successfully: {}", profile);

        return UserProfilesMapper.toUserResponse(profile);
    }

    @Override
    public UserProfileResponse getMe() {
        UUID userId = securityContextHelper.getCurrentUserId();
        UserProfiles profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        log.info("Get user profile successfully: {}", profile);

        return UserProfilesMapper.toUserResponse(profile);
    }

    @Override
    public PageResponse<UserProfileResponse> getList(int page, int size) {
        userProfileValidation.validatePagination(page, size);
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<UserProfiles> pageData = userProfileRepository.findAll(pageable);
        log.info("Get list user profile: {}", pageData);

        return PageResponse.<UserProfileResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(
                        pageData.getContent()
                                .stream()
                                .map(UserProfilesMapper::toUserResponse)
                                .toList()
                )
                .build();
    }

    @Override
    public UserProfileResponse update(UserUpdatedRequest request) {
        UUID userId = securityContextHelper.getCurrentUserId();
        UserProfiles profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getEmail() != null) profile.setEmail(request.getEmail());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        userProfileRepository.save(profile);
        log.info("User profile updated successfully: {}", profile);

        return UserProfilesMapper.toUserResponse(profile);
    }

    @Override
    public String delete(UUID id) {
        UserProfiles profile = userProfileRepository.findByUserId(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userProfileRepository.delete(profile);
        log.info("User profile deleted successfully: {}", profile);

        return "User profile deleted successfully";
    }
}
