package com.r2s.auth;

import com.r2s.auth.entity.Auth;
import com.r2s.auth.repository.AuthRepository;
import com.r2s.auth.service.impl.AuthServiceImpl;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.enums.Role;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private AuthRepository authRepository;

    @BeforeEach
    void setup() {
        authRepository.deleteAll();
    }

    @Test
    void register_with_username_exists() {
        Auth auth = Auth.builder()
                .username("username")
                .password("encoded")
                .role(Role.USER)
                .build();
        authRepository.save(auth);

        RegisterRequest request = RegisterRequest.builder()
                .username("username")
                .password("123")
                .build();

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.register(request)
        );

        assertEquals(ErrorCode.USER_EXISTS, ex.getErrorCode());
        assertEquals(1, authRepository.count());
    }

    @Test
    void register_success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("username")
                .password("123")
                .build();

        String result = authService.register(request);

        assertNotNull(result);
        assertTrue(result.contains("User created successfully"));

        Auth savedUser = authRepository.findByUsername("username").orElseThrow();
        assertEquals("username", savedUser.getUsername());
        assertEquals(Role.USER, savedUser.getRole());
        assertNotEquals("123", savedUser.getPassword());
    }

    @Test
    void login_with_user_not_found() {
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("123")
                .build();

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.login(request)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void login_with_invalid_password() {
        // tạo user
        RegisterRequest register = RegisterRequest.builder()
                .username("username")
                .password("123")
                .build();
        authService.register(register);

        LoginRequest login = LoginRequest.builder()
                .username("username")
                .password("wrong-password")
                .build();

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.login(login)
        );

        assertEquals(ErrorCode.PASSWORD_INVALID, ex.getErrorCode());
    }

    @Test
    void login_success() {
        RegisterRequest register = RegisterRequest.builder()
                .username("username")
                .password("123")
                .build();
        authService.register(register);

        LoginRequest login = LoginRequest.builder()
                .username("username")
                .password("123")
                .build();

        AuthResponse authResponse = authService.login(login);
        assertNotNull(authResponse);
        assertNotNull(authResponse.getToken());
        assertFalse(authResponse.getToken().isBlank());
    }
}
