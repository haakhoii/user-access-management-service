package com.r2s.auth;

import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Testcontainers
class AuthServiceE2ETest {
    private static final String BASE_URL = "http://localhost:8081/auth";

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("auth_e2e_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        registry.add("server.port", () -> 8081);
        registry.add("server.servlet.context-path", () -> "/auth");
    }
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void e2e_register_success() {
        String username = "e2e_user_" + UUID.randomUUID();

        RegisterRequest request = new RegisterRequest(username, "password", "user");

        ResponseEntity<ApiResponse<String>> response =
                restTemplate.exchange(
                        BASE_URL + "/register",
                        HttpMethod.POST,
                        new HttpEntity<>(request),
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResult())
                .contains("User created successfully");
    }

    @Test
    void e2e_register_username_exists() {
        String username = "e2e_dup_" + UUID.randomUUID();

        RegisterRequest request = new RegisterRequest(username, "password", "user");

        restTemplate.postForEntity(
                BASE_URL + "/register",
                request,
                Object.class
        );

        ResponseEntity<ApiResponse> response =
                restTemplate.postForEntity(
                        BASE_URL + "/register",
                        request,
                        ApiResponse.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode())
                .isEqualTo(ErrorCode.USER_EXISTS.getCode());
    }

    @Test
    void e2e_login_success() {
        String username = "e2e_login_" + UUID.randomUUID();
        String password = "password";

        restTemplate.postForEntity(
                BASE_URL + "/register",
                new RegisterRequest(username, password, "admin"),
                Object.class
        );

        LoginRequest loginRequest = new LoginRequest(username, password);

        ResponseEntity<ApiResponse<AuthResponse>> response =
                restTemplate.exchange(
                        BASE_URL + "/login",
                        HttpMethod.POST,
                        new HttpEntity<>(loginRequest),
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResult()).isNotNull();
        assertThat(response.getBody().getResult().getToken()).isNotBlank();
    }

    @Test
    void e2e_login_wrong_password() {
        String username = "e2e_wrong_" + UUID.randomUUID();
        String password = "password";

        restTemplate.postForEntity(
                BASE_URL + "/register",
                new RegisterRequest(username, password, ""),
                Object.class
        );

        LoginRequest loginRequest = new LoginRequest(username, "wrong-password");

        ResponseEntity<ApiResponse> response =
                restTemplate.postForEntity(
                        BASE_URL + "/login",
                        loginRequest,
                        ApiResponse.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode())
                .isEqualTo(ErrorCode.PASSWORD_INVALID.getCode());
    }
}
