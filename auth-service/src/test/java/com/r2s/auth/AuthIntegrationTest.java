package com.r2s.auth;

import com.r2s.auth.repository.AuthRepository;
import com.r2s.auth.service.impl.AuthServiceImpl;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.core.entity.User;
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
        User user = User.builder()
                .username("username")
                .password("encoded")
                .role(Role.USER)
                .build();
        authRepository.save(user);

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
                .fullName("Test User")
                .email("test@email.com")
                .build();

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("username", response.getUsername());
        assertEquals(Role.USER.name(), response.getRole());

        User savedUser = authRepository.findByUsername("username").orElseThrow();
        assertNotEquals("123", savedUser.getPassword()); // đã encode
    }

    @Test
    void login_with_user_not_found() {
        LoginRequest request = LoginRequest.builder()
                .username("username")
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
        RegisterRequest register = RegisterRequest.builder()
                .username("username")
                .password("123")
                .fullName("Test User")
                .email("test@email.com")
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
                .fullName("Test User")
                .email("test@email.com")
                .build();

        authService.register(register);

        LoginRequest login = LoginRequest.builder()
                .username("username")
                .password("123")
                .build();

        AuthResponse response = authService.login(login);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());
    }
}
