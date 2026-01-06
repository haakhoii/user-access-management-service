package com.r2s.auth_service.controller;

import com.r2s.auth_service.service.AuthService;
import com.r2s.core_service.dto.ApiResponse;
import com.r2s.core_service.dto.request.LoginRequest;
import com.r2s.core_service.dto.request.RegisterRequest;
import com.r2s.core_service.dto.response.TokenResponse;
import com.r2s.core_service.dto.response.AccountResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthService authService;

    @PostMapping("/register")
    ApiResponse<AccountResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.<AccountResponse>builder()
                .result(authService.register(request))
                .build();
    }

    @PostMapping("/login")
    ApiResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.<TokenResponse>builder()
                .result(authService.login(request))
                .build();
    }
}
