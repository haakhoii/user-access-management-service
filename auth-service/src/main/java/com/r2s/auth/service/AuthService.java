package com.r2s.auth.service;

import com.r2s.core.dto.request.IntrospectRequest;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.dto.response.IntrospectResponse;

public interface AuthService {
    String register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    IntrospectResponse introspect(IntrospectRequest request);
}