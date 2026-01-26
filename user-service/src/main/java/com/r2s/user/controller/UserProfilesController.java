package com.r2s.user.controller;

import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserProfileResponse;
import com.r2s.user.service.UserProfilesService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfilesController {
    UserProfilesService userProfilesService;

    @GetMapping("/hello")
    public String hello() {
        return "hello from user service";
    }

    @PostMapping("/create")
    ApiResponse<UserProfileResponse> create(@RequestBody UserCreatedRequest request) {
        log.info("User profile created request: {}", request);
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfilesService.create(request))
                .build();
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    ApiResponse<PageResponse<UserProfileResponse>> getList(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "4") int size
    ) {
        return ApiResponse.<PageResponse<UserProfileResponse>>builder()
                .result(userProfilesService.getList(page, size))
                .build();
    }

    @GetMapping("/me")
    ApiResponse<UserProfileResponse> getMe() {
        log.info("Get my profile request");
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfilesService.getMe())
                .build();
    }

    @PutMapping("/update")
    ApiResponse<UserProfileResponse> update(@RequestBody UserUpdatedRequest request) {
        log.info("Update profile request: {}", request);
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfilesService.update(request))
                .build();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> delete(@PathVariable("id") UUID id) {
        log.info("Delete user profile request with userId={}", id);
        return ApiResponse.<String>builder()
                .result(userProfilesService.delete(id))
                .build();
    }
}
