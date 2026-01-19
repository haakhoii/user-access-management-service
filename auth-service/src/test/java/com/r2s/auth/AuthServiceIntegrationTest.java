package com.r2s.auth;

import com.r2s.auth.service.AuthService;
import com.r2s.core.dto.request.IntrospectRequest;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.dto.response.IntrospectResponse;
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
class AuthServiceIntegrationTest {

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
    AuthService authService;

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest(
                "user_integration",
                "password",
                ""
        );
        String result = authService.register(request);
        assertThat(result).contains("User created successfully");
    }

    @Test
    void register_username_exists() {
        RegisterRequest request = new RegisterRequest(
                "alice",
                "password",
                ""
        );
        authService.register(request);

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.register(request)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_EXISTS);
    }


    @Test
    void login_success() {
        authService.register(
                new RegisterRequest("user_integration", "password", "")
        );
        LoginRequest loginReq = new LoginRequest(
                "user_integration",
                "password"
        );

        AuthResponse response = authService.login(loginReq);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    void login_wrong_password() {
        authService.register(
                new RegisterRequest("user_integration", "password", "")
        );

        LoginRequest loginReq = new LoginRequest(
                "user_integration",
                "wrong-password"
        );

        AppException ex = assertThrows(
                AppException.class,
                () -> authService.login(loginReq)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_INVALID);
    }
}
