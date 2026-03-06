package com.r2s.auth.test.integration;

import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.service.AuthenticationService;
import com.r2s.auth.service.UserService;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
    properties = "SPRING_PROFILES_ACTIVE=test"
)
@ActiveProfiles("test")
@Testcontainers
@Transactional
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("auth_test_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("jwt.signerKey", () ->
            UUID.randomUUID().toString().repeat(4)
        );
        registry.add("jwt.expiry", () -> "15");
    }

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    // happy case
    @Test
    void register_success() {
        RegisterRequest request = RegisterRequest.builder()
            .username("user_register")
            .password("password")
            .role("")
            .build();
        String result = userService.register(request);

        assertThat(result).contains("User created");
    }

    @Test
    void getMe_success() {
        RegisterRequest request = RegisterRequest.builder()
            .username("user_me")
            .password("password")
            .role("")
            .build();
        userService.register(request);
        UUID userId = userRepository
            .findByUsername("user_me")
            .orElseThrow()
            .getId();
        Jwt jwt = Jwt.withTokenValue("test")
            .header("alg", "HS512")
            .claim("sub", userId.toString())
            .build();
        SecurityContextHolder.getContext()
            .setAuthentication(new JwtAuthenticationToken(jwt));
        var user = userService.getMe();

        assertThat(user.getUsername()).isEqualTo("user_me");
    }

    // negative case
    @Test
    void register_userExists_throwException() {
        RegisterRequest request = RegisterRequest.builder()
            .username("user_exists")
            .password("password")
            .role("")
            .build();
        userService.register(request);
        AppException ex = assertThrows(
            AppException.class,
            () -> userService.register(request)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_EXISTS);
    }

    @Test
    void register_validationFailure_throwException() {
        RegisterRequest request = RegisterRequest.builder()
            .username(null)
            .password(null)
            .build();
        AppException ex = assertThrows(
            AppException.class,
            () -> userService.register(request)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void register_roleNotFound_throwException() {
        RegisterRequest request = RegisterRequest.builder()
            .username("user_role_fail")
            .password("password")
            .role("ROLE_NOT_FOUND")
            .build();
        AppException ex = assertThrows(
            AppException.class,
            () -> userService.register(request)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_FOUND);
    }

    @Test
    void getMe_unauthorized_throwException() {
        SecurityContextHolder.clearContext();
        AppException ex = assertThrows(
            AppException.class,
            () -> userService.getMe()
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void getMe_userNotFound_throwException() {
        UUID randomUserId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("test")
            .header("alg", "HS512")
            .claim("sub", randomUserId.toString())
            .build();
        SecurityContextHolder.getContext()
            .setAuthentication(new JwtAuthenticationToken(jwt));
        AppException ex = assertThrows(
            AppException.class,
            () -> userService.getMe()
        );

        assertThat(ex.getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

}