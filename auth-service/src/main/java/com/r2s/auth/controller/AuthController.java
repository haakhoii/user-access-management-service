package com.r2s.auth.controller;

import com.r2s.auth.service.AuthService;
import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.request.UpdateUserRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthService authService;

    @PostMapping("/register")
    ApiResponse<UserResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(authService.register(request))
                .build();
    }

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .result(authService.login(request))
                .build();
    }

    @PutMapping("/update")
    ApiResponse<UserResponse> update(@RequestBody UpdateUserRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(authService.update(request))
                .build();
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> delete() {
        return ApiResponse.<String>builder()
                .result(authService.delete())
                .build();
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    ApiResponse<PageResponse<UserResponse>> getList(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "4") int size
    ) {
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .result(authService.getList(page, size))
                .build();
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> getMe() {
        return ApiResponse.<UserResponse>builder()
                .result(authService.getMe())
                .build();
    }
}
