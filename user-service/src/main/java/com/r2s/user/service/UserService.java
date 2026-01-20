package com.r2s.user.service;

import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {
    UserResponse create(UserCreatedRequest request);

    PageResponse<UserResponse> getList(int page, int size);

    UserResponse getMe();

    UserResponse update(UserUpdatedRequest request);

    String delete(UUID id);
}
