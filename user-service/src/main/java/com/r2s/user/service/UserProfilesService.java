package com.r2s.user.service;

import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserProfileResponse;

import java.util.UUID;

public interface UserProfilesService {
    UserProfileResponse create(UserCreatedRequest request);

    PageResponse<UserProfileResponse> getList(int page, int size);

    UserProfileResponse getMe();

    UserProfileResponse update(UserUpdatedRequest request);

    String delete(UUID id);
}
