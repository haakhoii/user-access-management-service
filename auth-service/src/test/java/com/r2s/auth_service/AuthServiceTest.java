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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    AuthRepository authRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtToken jwtToken;

    @InjectMocks
    AuthService authService;

    @Test
    void register_with_username_exists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("username")
                .password("123")
                .confirmPassword("123")
                .build();

        when(authRepository.findByUsername("username"))
                .thenReturn(Optional.of(new Auth()));
        AppException appException = assertThrows(
                AppException.class,
                () -> authService.register(request)
        );
        assertEquals(ErrorCode.USER_EXISTS, appException.getErrorCode());
        verify(authRepository, never()).save(any());
    }

    @Test
    void register_with_an_invalid_password() {
        RegisterRequest request = RegisterRequest.builder()
                .username("username")
                .password("123")
                .confirmPassword("456")
                .build();

        AppException appException = assertThrows(
                AppException.class,
                () -> authService.register(request)
        );
        assertEquals(ErrorCode.PASSWORD_INVALID, appException.getErrorCode());
        verify(authRepository, never()).save(any());
    }

    @Test
    void register_success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("username")
                .password("123")
                .confirmPassword("123")
                .build();
        Auth auth = Auth.builder()
                .id("1")
                .username("username")
                .password("encoded")
                .role(Role.USER)
                .build();

        when(passwordEncoder.encode("123")).thenReturn("encoded");
        when(authRepository.save(any(Auth.class))).thenReturn(auth);

        AccountResponse response = authService.register(request);

        assertEquals("username", response.getUsername());
        verify(authRepository).save(any(Auth.class));
    }

    @Test
    void login_with_user_not_found() {
        LoginRequest request = LoginRequest.builder()
                .username("username")
                .password("123")
                .build();

        when(authRepository.findByUsername("username"))
                .thenReturn(Optional.empty());

        AppException appException = assertThrows(
                AppException.class,
                () -> authService.login(request)
        );
        assertEquals(ErrorCode.USER_NOT_FOUND, appException.getErrorCode());
        verify(authRepository, never()).save(any());
    }

    @Test
    void login_with_an_invalid_password() {
        LoginRequest request = LoginRequest.builder()
                .username("username")
                .password("123")
                .build();

        Auth auth = Auth.builder()
                .id("1")
                .username("username")
                .password("encode")
                .role(Role.USER)
                .build();
        when(authRepository.findByUsername("username"))
                .thenReturn(Optional.of(auth));
        when(passwordEncoder.matches(eq("123"), anyString()))
                .thenReturn(false);
        AppException appException = assertThrows(
                AppException.class,
                () -> authService.login(request)
        );
        assertEquals(ErrorCode.PASSWORD_INVALID, appException.getErrorCode());
        verify(authRepository, never()).save(any());
    }

    @Test
    void login_success() {
        LoginRequest request = LoginRequest.builder()
                .username("username")
                .password("123")
                .build();

        Auth auth = Auth.builder()
                .id("1")
                .username("username")
                .password("encode")
                .role(Role.USER)
                .build();

        when(authRepository.findByUsername("username"))
                .thenReturn(Optional.of(auth));
        when(passwordEncoder.matches(eq("123"), anyString()))
                .thenReturn(true);
        when(jwtToken.generateToken(auth))
                .thenReturn("token");
        when(jwtToken.generateExpiry())
                .thenReturn(Instant.now());

        TokenResponse tokenResponse = authService.login(request);
        assertNotNull(tokenResponse.getToken());
    }
}
