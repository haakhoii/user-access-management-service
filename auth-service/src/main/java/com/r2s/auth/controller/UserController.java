package com.r2s.auth.controller;

import com.r2s.auth.service.UserService;
import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "User",
        description = "Register and getMe"
)
public class UserController {
    UserService userService;

    @Operation(
            summary = "Register",
            description = "Create a user using a username and password"
    )
    @PostMapping("/register")
    ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register with username: {}", request.getUsername());
        return ApiResponse.<String>builder()
                .result(userService.register(request))
                .build();
    }

    @Operation(
            summary = "Get Me",
            description = "Get user by token"
    )
    @GetMapping("/me")
    ApiResponse<UserResponse> me() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMe())
                .build();
    }

}