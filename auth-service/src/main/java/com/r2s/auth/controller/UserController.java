package com.r2s.auth.controller;

import com.r2s.auth.service.AuthenticationService;
import com.r2s.auth.service.UserService;
import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.IntrospectRequest;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;

    @PostMapping("/register")
    ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register with username: {}", request.getUsername());
        return ApiResponse.<String>builder()
                .result(userService.register(request))
                .build();
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> me() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMe())
                .build();
    }

}