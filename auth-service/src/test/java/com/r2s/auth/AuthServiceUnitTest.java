package com.r2s.auth;

import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.auth.repository.AuthRepository;
import com.r2s.auth.repository.RoleRepository;
import com.r2s.auth.service.impl.AuthServiceImpl;
import com.r2s.auth.token.JwtToken;
import com.r2s.core.dto.request.IntrospectRequest;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    AuthRepository authRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtToken jwtToken;

    @InjectMocks
    AuthServiceImpl authService;

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest("alice", "password");

        when(authRepository.findByUsername("alice"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password"))
                .thenReturn("encoded-password");

        Role roleUser = Role.builder()
                .id(1)
                .name("ROLE_USER")
                .build();

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(roleUser));

        String result = authService.register(request);

        assertThat(result).contains("User created successfully");

        verify(authRepository).save(any(User.class));
    }

    @Test
    void register_username_exists() {
        RegisterRequest request = new RegisterRequest("bob", "password");

        when(authRepository.findByUsername("bob"))
                .thenReturn(Optional.of(new User()));

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.register(request)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_EXISTS);

        verify(authRepository, never()).save(any());
    }

    @Test
    void login_success() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("charlie")
                .password("encoded-password")
                .roles(Set.of(Role.builder().name("ROLE_USER").build()))
                .build();

        when(authRepository.findByUsername("charlie"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "encoded-password"))
                .thenReturn(true);

        when(jwtToken.generateToken(user))
                .thenReturn(new AuthResponse("jwt-token"));

        AuthResponse response = authService.login(
                new LoginRequest("charlie", "password")
        );

        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_wrong_password() {
        User user = User.builder()
                .username("david")
                .password("encoded-password")
                .build();

        when(authRepository.findByUsername("david"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "encoded-password"))
                .thenReturn(false);

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.login(new LoginRequest("david", "wrong"))
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_INVALID);
    }

    @Test
    void introspect_valid_token() {
        Jwt jwt = Jwt.withTokenValue("token")
                .subject("user-id")
                .claim("username", "eva")
                .claim("scope", "ROLE_USER")
                .expiresAt(Instant.now().plusSeconds(600))
                .header("alg", "HS512")
                .build();

        when(jwtToken.verify("token"))
                .thenReturn(jwt);

        IntrospectResponse response =
                authService.introspect(new IntrospectRequest("token"));

        assertThat(response.isValid()).isTrue();
        assertThat(response.getUsername()).isEqualTo("eva");
        assertThat(response.getScope()).contains("ROLE_USER");
    }
}
