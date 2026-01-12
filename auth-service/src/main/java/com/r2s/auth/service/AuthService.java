package com.r2s.auth.service;

import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.request.UpdateUserRequest;
import com.r2s.core.dto.response.AuthResponse;

import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse update(UpdateUserRequest request);

    String delete();

    PageResponse<UserResponse> getList(int page, int size);

    UserResponse getMe();

//    IntrospectResponse introspect(IntrospectRequest request);
}
