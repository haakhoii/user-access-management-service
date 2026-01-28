package com.r2s.auth.service;

import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.dto.response.TokenResponse;

public interface AuthenticationService {
    TokenResponse login(LoginRequest request);

    IntrospectResponse introspect();
}
