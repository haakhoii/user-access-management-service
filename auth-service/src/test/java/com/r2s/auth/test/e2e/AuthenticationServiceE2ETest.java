package com.r2s.auth.test.e2e;

import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.dto.response.TokenResponse;
import com.r2s.core.dto.response.UserResponse;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class AuthenticationServiceE2ETest {

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

    @Test
    void flow_e2e_register_login_introspect_me() {
//        register
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("e2e_user")
                .password("password")
                .role("")
                .build();

        ResponseEntity<ApiResponse<String>> registerRes =
                restTemplate.exchange(
                        "/register",
                        HttpMethod.POST,
                        new HttpEntity<>(registerRequest),
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(registerRes.getStatusCode()).isEqualTo(HttpStatus.OK);

//        login
        LoginRequest loginRequest = LoginRequest.builder()
                .username("e2e_user")
                .password("password")
                .build();

        ResponseEntity<ApiResponse<TokenResponse>> loginRes =
                restTemplate.exchange(
                        "/login",
                        HttpMethod.POST,
                        new HttpEntity<>(loginRequest),
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        TokenResponse token = loginRes.getBody().getResult();
        assertThat(token).isNotNull();
        assertThat(token.getToken()).isNotBlank();

        String bearerToken = "Bearer " + token.getToken();

//        introspect
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<Void> authEntity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<IntrospectResponse>> introspectRes =
                restTemplate.exchange(
                        "/introspect",
                        HttpMethod.POST,
                        authEntity,
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(introspectRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        IntrospectResponse introspect = introspectRes.getBody().getResult();

        assertThat(introspect.getUsername()).isEqualTo("e2e_user");

//        get me
        ResponseEntity<ApiResponse<UserResponse>> meRes =
                restTemplate.exchange(
                        "/me",
                        HttpMethod.GET,
                        authEntity,
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(meRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        UserResponse me = meRes.getBody().getResult();
        assertThat(me.getUsername()).isEqualTo("e2e_user");
    }
}
