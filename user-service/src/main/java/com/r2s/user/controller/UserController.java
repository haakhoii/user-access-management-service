package com.r2s.user.controller;

import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.core.dto.response.CursorResponse;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;

    @GetMapping("/hello")
    public String hello() {
        return "hello from user service";
    }

    @PostMapping("/create")
    ApiResponse<UserResponse> create(@RequestBody UserCreatedRequest request) {
        log.info("User profile created request: {}", request);
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

    @GetMapping("/list/cursor")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    ApiResponse<CursorResponse<UserResponse>> getList(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        LocalDateTime cursorTime = cursor != null ? LocalDateTime.parse(cursor) : null;
        log.info("Get user list cursor={}, size={}", cursor, size);
        return ApiResponse.<CursorResponse<UserResponse>>builder()
                .result(userService.getListCursor(cursorTime, size))
                .build();
    }


    @GetMapping("/me")
    ApiResponse<UserResponse> getMe() {
        log.info("Get my profile request");
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMe())
                .build();
    }

    @PutMapping("/update")
    ApiResponse<UserResponse> update(@RequestBody UserUpdatedRequest request) {
        log.info("Update profile request: {}", request);
        return ApiResponse.<UserResponse>builder()
                .result(userService.update(request))
                .build();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> delete(@PathVariable("id") UUID id) {
        log.info("Delete user profile request with userId={}", id);
        return ApiResponse.<String>builder()
                .result(userService.delete(id))
                .build();
    }
}
