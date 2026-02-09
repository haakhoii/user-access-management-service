package com.r2s.auth.test.integration;

import com.r2s.auth.service.AuthenticationService;
import com.r2s.auth.service.UserService;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.TokenResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
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
class AuthenticationServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("auth_test_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        // db
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        // jwt
        registry.add("jwt.signerKey", () ->
                UUID.randomUUID().toString().repeat(4)
        );
        registry.add("jwt.expiry", () -> "15");

        // redis
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    UserService userService;

    @Test
    void login_success() {
        userService.register(
                RegisterRequest.builder()
                        .username("auth_user")
                        .password("password")
                        .role("")
                        .build()
        );

        LoginRequest request = LoginRequest.builder()
                .username("auth_user")
                .password("password")
                .build();

        TokenResponse response = authenticationService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    void login_wrongPassword_throwException() {
        userService.register(
                RegisterRequest.builder()
                        .username("auth_fail")
                        .password("password")
                        .role("")
                        .build()
        );

        LoginRequest request = LoginRequest.builder()
                .username("auth_fail")
                .password("wrong-password")
                .build();

        AppException ex = assertThrows(
                AppException.class,
                () -> authenticationService.login(request)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_INVALID);
    }

    @Test
    void introspect_userNotFound_throwException() {
        AppException ex = assertThrows(
                AppException.class,
                () -> authenticationService.introspect()
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void introspect_invalidToken_throwUnauthorized() {
        AppException ex = assertThrows(
                AppException.class,
                () -> authenticationService.introspect()
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void login_rateLimitExceeded_throwException() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("rate_user")
                .password("password")
                .build();

        userService.register(registerRequest);

        LoginRequest request = LoginRequest.builder()
                .username("rate_user")
                .password("wrong")
                .build();

        AppException ex = assertThrows(
                AppException.class,
                () -> authenticationService.login(request)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_INVALID);
    }

}
