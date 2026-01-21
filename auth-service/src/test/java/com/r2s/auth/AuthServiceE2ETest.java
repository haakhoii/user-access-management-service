package com.r2s.auth;

import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.dto.response.MeResponse;
import com.r2s.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
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

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // happy case
    @Test
    void e2e_register_login_get_me_success() {
        String username = "e2e_" + UUID.randomUUID();
        String password = "123456";

        // register
        ResponseEntity<ApiResponse<String>> registerRes =
                restTemplate.exchange(
                        url("/register"),
                        HttpMethod.POST,
                        new HttpEntity<>(new RegisterRequest(username, password, "user")),
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(registerRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        // login
        ResponseEntity<ApiResponse<AuthResponse>> loginRes =
                restTemplate.exchange(
                        url("/login"),
                        HttpMethod.POST,
                        new HttpEntity<>(new LoginRequest(username, password)),
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        String token = loginRes.getBody().getResult().getToken();
        assertThat(token).isNotBlank();

        // get Me
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<ApiResponse<MeResponse>> meRes =
                restTemplate.exchange(
                        url("/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(meRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(meRes.getBody()).isNotNull();
        assertThat(meRes.getBody().getResult().getUsername()).isEqualTo(username);
        assertThat(meRes.getBody().getResult().getRoles()).contains("ROLE_USER");
    }

    // negative case
    @Test
    void e2e_get_me_without_token_unauthorized() {
        ResponseEntity<ApiResponse> response =
                restTemplate.getForEntity(url("/me"), ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void e2e_register_duplicate_username() {
        String username = "dup_" + UUID.randomUUID();
        RegisterRequest req = new RegisterRequest(username, "123456", "user");

        restTemplate.postForEntity(url("/register"), req, Object.class);

        ResponseEntity<ApiResponse> response =
                restTemplate.postForEntity(url("/register"), req, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode())
                .isEqualTo(ErrorCode.USER_EXISTS.getCode());
    }

    @Test
    void e2e_login_wrong_password() {
        String username = "wrong_" + UUID.randomUUID();

        restTemplate.postForEntity(
                url("/register"),
                new RegisterRequest(username, "123456", "user"),
                Object.class
        );

        LoginRequest login = new LoginRequest(username, "wrong");

        ResponseEntity<ApiResponse> response =
                restTemplate.postForEntity(url("/login"), login, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getCode())
                .isEqualTo(ErrorCode.PASSWORD_INVALID.getCode());
    }
}
