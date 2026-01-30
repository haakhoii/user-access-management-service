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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
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

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
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
}
