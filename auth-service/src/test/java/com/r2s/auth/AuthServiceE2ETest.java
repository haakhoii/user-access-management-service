package com.r2s.auth;

import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.IntrospectRequest;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class AuthServiceE2ETest {

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
    }

    @Autowired
    TestRestTemplate restTemplate;

    private static final String BASE_URL = "";

    @Test
    void e2e_register_success() {
        RegisterRequest request = new RegisterRequest(
                "e2e_user",
                "@P4ssw0rd"
        );

        ResponseEntity<ApiResponse> response =
                restTemplate.postForEntity(
                        BASE_URL + "/register",
                        request,
                        ApiResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getResult().toString())
                .contains("User created successfully");
    }

    @Test
    void e2e_register_username_exists() {
        RegisterRequest request = new RegisterRequest(
                "e2e_exists",
                "@P4ssw0rd"
        );

        // lần 1: tạo user thành công
        ResponseEntity<ApiResponse> first =
                restTemplate.postForEntity(
                        BASE_URL + "/register",
                        request,
                        ApiResponse.class
                );

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);

        // lần 2: trùng username
        ResponseEntity<ApiResponse> second =
                restTemplate.postForEntity(
                        BASE_URL + "/register",
                        request,
                        ApiResponse.class
                );

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ApiResponse body = second.getBody();
        assertThat(body).isNotNull();

        // tuỳ ApiResponse của bạn có code hay message
        assertThat(body.getCode()).isEqualTo(ErrorCode.USER_EXISTS.getCode());
    }


    @Test
    void e2e_login_success() {
        restTemplate.postForEntity(
                BASE_URL + "/register",
                new RegisterRequest("e2e_login", "@P4ssw0rd"),
                ApiResponse.class
        );

        LoginRequest loginReq = new LoginRequest(
                "e2e_login",
                "@P4ssw0rd"
        );

        ResponseEntity<ApiResponse> response =
                restTemplate.postForEntity(
                        BASE_URL + "/login",
                        loginReq,
                        ApiResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getResult().toString())
                .contains("token");
    }

    @Test
    void e2e_login_wrong_password() {
        restTemplate.postForEntity(
                BASE_URL + "/register",
                new RegisterRequest("e2e_wrong", "@P4ssw0rd"),
                ApiResponse.class
        );

        LoginRequest loginReq = new LoginRequest(
                "e2e_wrong",
                "wrong-password"
        );

        ResponseEntity<ApiResponse> response =
                restTemplate.postForEntity(
                        BASE_URL + "/login",
                        loginReq,
                        ApiResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void e2e_introspect_valid_token() {
        restTemplate.postForEntity(
                "/register",
                new RegisterRequest("e2e_intro", "@P4ssw0rd"),
                ApiResponse.class
        );

        ResponseEntity<ApiResponse> loginRes =
                restTemplate.postForEntity(
                        "/login",
                        new LoginRequest("e2e_intro", "@P4ssw0rd"),
                        ApiResponse.class
                );

        @SuppressWarnings("unchecked")
        Map<String, Object> loginResult = (Map<String, Object>) loginRes.getBody().getResult();

        String token = loginResult.get("token").toString();

        ResponseEntity<ApiResponse> response =
                restTemplate.postForEntity(
                        "/introspect",
                        new IntrospectRequest(token),
                        ApiResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getResult().toString())
                .contains("ROLE_USER");
    }

}
