package com.r2s.auth.test.integration;

import com.r2s.auth.service.AuthenticationService;
import com.r2s.auth.service.UserService;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
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
    }

    @Autowired
    UserService userService;

    @Autowired
    AuthenticationService authenticationService;

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
    void getMe_unauthorized_throwException() {
        SecurityContextHolder.clearContext();
        AppException ex = assertThrows(
                AppException.class,
                () -> userService.getMe()
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
    }
}
