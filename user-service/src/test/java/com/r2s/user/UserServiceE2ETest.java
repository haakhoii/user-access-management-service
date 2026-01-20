package com.r2s.user;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.r2s.core.dto.ApiResponse;
import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.user.entity.UserProfiles;
import com.r2s.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Testcontainers
class UserServiceE2ETest {

    private static final String BASE_URL = "http://localhost:8082";

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("user_e2e_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        registry.add("server.port", () -> 8082);
        registry.add("server.servlet.context-path", () -> "/user");
    }

    @Value("${jwt.signer-key}")
    String signerKey;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UserRepository userRepository;

    UUID userId;
    UUID adminId;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        userId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        userRepository.save(
                UserProfiles.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .username("e2e_user")
                        .roles("ROLE_USER")
                        .build()
        );

        userRepository.save(
                UserProfiles.builder()
                        .id(UUID.randomUUID())
                        .userId(adminId)
                        .username("e2e_admin")
                        .roles("ROLE_ADMIN")
                        .build()
        );
    }

    private String generateJwt(UUID userId, String username, String role) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .claim("username", username)
                    .claim("scope", "ROLE_" + role)
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS512),
                    claims
            );

            jwt.sign(new MACSigner(signerKey.getBytes()));
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpHeaders authHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void e2e_create_user_success() {
        UUID newUserId = UUID.randomUUID();
        String token = generateJwt(newUserId, "new_user", "USER");

        UserCreatedRequest request = new UserCreatedRequest();
        request.setFullName("New User");
        request.setEmail("new@test.com");
        request.setPhone("0123456789");
        request.setAddress("HCM");
        request.setAvatarUrl("avatar.png");

        ResponseEntity<ApiResponse> response =
                restTemplate.exchange(
                        BASE_URL + "/user/create",
                        HttpMethod.POST,
                        new HttpEntity<>(request, authHeader(token)),
                        ApiResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void e2e_get_me_success() {
        String token = generateJwt(userId, "e2e_user", "USER");

        ResponseEntity<ApiResponse> response =
                restTemplate.exchange(
                        BASE_URL + "/user/me",
                        HttpMethod.GET,
                        new HttpEntity<>(authHeader(token)),
                        ApiResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void e2e_update_me_success() {
        String token = generateJwt(userId, "e2e_user", "USER");

        UserUpdatedRequest request = new UserUpdatedRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@test.com");
        request.setPhone("0999999999");
        request.setAddress("HN");
        request.setAvatarUrl("updated.png");

        ResponseEntity<ApiResponse> response =
                restTemplate.exchange(
                        BASE_URL + "/user/update",
                        HttpMethod.PUT,
                        new HttpEntity<>(request, authHeader(token)),
                        ApiResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void e2e_get_list_by_admin_success() {
        String token = generateJwt(adminId, "e2e_admin", "ADMIN");

        ResponseEntity<ApiResponse> response =
                restTemplate.exchange(
                        BASE_URL + "/user/list?page=1&size=5",
                        HttpMethod.GET,
                        new HttpEntity<>(authHeader(token)),
                        ApiResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void e2e_delete_user_by_admin_success() {
        String token = generateJwt(adminId, "e2e_admin", "ADMIN");

        UUID deleteUserId =
                userRepository.findAll().stream()
                        .filter(u -> u.getRoles().contains("ROLE_USER"))
                        .findFirst()
                        .orElseThrow()
                        .getUserId();

        ResponseEntity<ApiResponse> response =
                restTemplate.exchange(
                        BASE_URL + "/user/delete/" + deleteUserId,
                        HttpMethod.DELETE,
                        new HttpEntity<>(authHeader(token)),
                        ApiResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
