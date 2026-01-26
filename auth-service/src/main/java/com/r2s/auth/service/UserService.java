package com.r2s.auth.service;

import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.UserResponse;

public interface UserService {
    String register(RegisterRequest request);

    UserResponse getMe();
}