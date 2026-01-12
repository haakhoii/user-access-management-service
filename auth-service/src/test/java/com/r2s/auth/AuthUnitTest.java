package com.r2s.auth;

import com.r2s.auth.repository.AuthRepository;
import com.r2s.auth.service.impl.AuthServiceImpl;
import com.r2s.auth.token.JwtToken;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.core.entity.User;
import com.r2s.core.enums.Role;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUnitTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtToken jwtToken;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_with_username_exists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("username")
                .password("123")
                .build();

        when(authRepository.findByUsername("username"))
                .thenReturn(Optional.of(User.builder().build()));

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.register(request)
        );

        assertEquals(ErrorCode.USER_EXISTS, ex.getErrorCode());
        verify(authRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void register_success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("username")
                .password("123")
                .fullName("Test User")
                .email("test@email.com")
                .build();

        when(authRepository.findByUsername("username"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("123"))
                .thenReturn("encoded");

        when(authRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("username", response.getUsername());
        assertEquals(Role.USER.name(), response.getRole());

        verify(authRepository).save(any(User.class));
        verify(passwordEncoder).encode("123");
    }

    @Test
    void login_with_user_not_found() {
        LoginRequest request = LoginRequest.builder()
                .username("username")
                .password("123")
                .build();

        when(authRepository.findByUsername("username"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.login(request)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        verify(jwtToken, never()).generateToken(any());
    }

    @Test
    void login_with_invalid_password() {
        LoginRequest request = LoginRequest.builder()
                .username("username")
                .password("123")
                .build();

        User user = User.builder()
                .id("1")
                .username("username")
                .password("encoded")
                .role(Role.USER)
                .build();

        when(authRepository.findByUsername("username"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123", "encoded"))
                .thenReturn(false);

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.login(request)
        );

        assertEquals(ErrorCode.PASSWORD_INVALID, ex.getErrorCode());
        verify(jwtToken, never()).generateToken(any());
    }

    @Test
    void login_success() {
        LoginRequest request = LoginRequest.builder()
                .username("username")
                .password("123")
                .build();

        User user = User.builder()
                .id("1")
                .username("username")
                .password("encoded")
                .role(Role.USER)
                .build();

        AuthResponse mockToken = AuthResponse.builder()
                .token("mock-token")
                .build();

        when(authRepository.findByUsername("username"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123", "encoded"))
                .thenReturn(true);

        when(jwtToken.generateToken(user))
                .thenReturn(mockToken);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
    }
}
