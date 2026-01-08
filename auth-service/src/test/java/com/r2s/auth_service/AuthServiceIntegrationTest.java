package com.r2s.auth_service;

import com.r2s.auth_service.config.JwtToken;
import com.r2s.auth_service.entity.Auth;
import com.r2s.auth_service.repository.AuthRepository;
import com.r2s.auth_service.service.AuthService;
import com.r2s.core_service.dto.request.LoginRequest;
import com.r2s.core_service.dto.request.RegisterRequest;
import com.r2s.core_service.dto.response.AccountResponse;
import com.r2s.core_service.dto.response.TokenResponse;
import com.r2s.core_service.enums.Role;
import com.r2s.core_service.exception.AppException;
import com.r2s.core_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtToken jwtToken;

    @Test
    void register_with_username_exists_should_throw_exception() {
        Auth existingUser = Auth.builder()
                .username("existing_user")
                .password("encoded")
                .role(Role.USER)
                .build();
        authRepository.save(existingUser);

        RegisterRequest request = RegisterRequest.builder()
                .username("existing_user")
                .password("123")
                .confirmPassword("123")
                .build();

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.register(request)
        );

        assertEquals(ErrorCode.USER_EXISTS, ex.getErrorCode());
    }

    @Test
    void register_with_invalid_password_should_throw_exception() {
        RegisterRequest request = RegisterRequest.builder()
                .username("new_user")
                .password("123")
                .confirmPassword("456")
                .build();

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.register(request)
        );

        assertEquals(ErrorCode.PASSWORD_INVALID, ex.getErrorCode());
    }

    @Test
    void register_success_should_persist_user() {
        RegisterRequest request = RegisterRequest.builder()
                .username("new_user")
                .password("123")
                .confirmPassword("123")
                .build();

        AccountResponse response = authService.register(request);

        assertEquals("new_user", response.getUsername());
        assertTrue(authRepository.findByUsername("new_user").isPresent());
    }


    @Test
    void login_with_user_not_found_should_throw_exception() {
        LoginRequest request = LoginRequest.builder()
                .username("non_exist_user")
                .password("123")
                .build();

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.login(request)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void login_with_invalid_password_should_throw_exception() {
        Auth user = Auth.builder()
                .username("login_user")
                .password(passwordEncoder.encode("123"))
                .role(Role.USER)
                .build();
        authRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .username("login_user")
                .password("wrong_password")
                .build();

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.login(request)
        );

        assertEquals(ErrorCode.PASSWORD_INVALID, ex.getErrorCode());
    }

    @Test
    void login_success_should_return_token() {
        Auth user = Auth.builder()
                .username("login_success_user")
                .password(passwordEncoder.encode("123"))
                .role(Role.USER)
                .build();
        authRepository.save(user);

        when(jwtToken.generateToken(any())).thenReturn("mocked-token");
        when(jwtToken.generateExpiry()).thenReturn(Instant.now());

        LoginRequest request = LoginRequest.builder()
                .username("login_success_user")
                .password("123")
                .build();

        TokenResponse tokenResponse = authService.login(request);

        assertEquals("mocked-token", tokenResponse.getToken());
        assertNotNull(tokenResponse.getExpiry());
    }
}
