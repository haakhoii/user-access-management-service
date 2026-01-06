package com.r2s.user_service.controller;

import com.r2s.core_service.dto.ApiResponse;
import com.r2s.core_service.dto.request.UserCreationRequest;
import com.r2s.core_service.dto.request.UserUpdatedRequest;
import com.r2s.core_service.dto.response.PageResponse;
import com.r2s.core_service.dto.response.UserResponse;
import com.r2s.user_service.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @GetMapping("/hello")
    public String hello() {
        return "hello from user service";
    }

    @PostMapping("/create")
    ApiResponse<UserResponse> create(@RequestBody UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.create(request))
                .build();
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    ApiResponse<PageResponse<UserResponse>> getList(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "4") int size
    ) {
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .result(userService.getList(page, size))
                .build();
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> getMe() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMe())
                .build();
    }

    @PutMapping("/upd")
    ApiResponse<UserResponse> update(
            @RequestBody UserUpdatedRequest request
    ) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.update(request))
                .build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> delete(@PathVariable("userId") String userId) {
        return ApiResponse.<String>builder()
                .result(userService.delete(userId))
                .build();
    }
}
