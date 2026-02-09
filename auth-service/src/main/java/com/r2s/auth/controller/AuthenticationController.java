package com.r2s.auth.controller;

import com.r2s.auth.service.AuthenticationService;
import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(
        name = "Authentication",
        description = "Login and introspection"
)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @Operation(
            summary = "Login",
            description = "Login by username and password"
    )
    @PostMapping("/login")
    ApiResponse<TokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login attempt with username: {}", request.getUsername());

        return ApiResponse.<TokenResponse>builder()
                .result(authenticationService.login(request))
                .build();
    }

    @Operation(
            summary = "Introspect token",
            description = "Check valid token"
    )
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect() {
        log.info("Introspect token request");
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect())
                .build();
    }
}
