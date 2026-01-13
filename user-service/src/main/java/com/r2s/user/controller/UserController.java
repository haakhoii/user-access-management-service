package com.r2s.user.controller;

import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.user.service.UserService;
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
    ApiResponse<UserResponse> create(@RequestBody UserCreatedRequest request) {
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

    @PutMapping("/update")
    ApiResponse<UserResponse> update(@RequestBody UserUpdatedRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.update(request))
                .build();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> delete(@PathVariable("id") String id) {
        return ApiResponse.<String>builder()
                .result(userService.delete(id))
                .build();
    }
}
