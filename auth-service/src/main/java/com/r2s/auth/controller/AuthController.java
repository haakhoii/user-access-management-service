package com.r2s.auth.controller;

import com.r2s.auth.service.AuthService;
import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.IntrospectRequest;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.dto.response.MeResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthController {
    AuthService authService;

    @PostMapping("/register")
    ApiResponse<String> register(@RequestBody RegisterRequest request) {
        log.info("Register with request: {}", request);
        return ApiResponse.<String>builder()
                .result(authService.register(request))
                .build();
    }

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt with username: {}", request.getUsername());
        return ApiResponse.<AuthResponse>builder()
                .result(authService.login(request))
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        log.info("Introspect token request");
        return ApiResponse.<IntrospectResponse>builder()
                .result(authService.introspect(request))
                .build();
    }

    @GetMapping("/me")
    ApiResponse<MeResponse> me() {
        return ApiResponse.<MeResponse>builder()
                .result(authService.getMe())
                .build();
    }

}