package com.r2s.user;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.r2s.core.dto.ApiResponse;
import com.r2s.user.entity.UserProfiles;
import com.r2s.user.repository.UserRepository;
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

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Testcontainers
class UserServiceE2ETest {
    private static final String BASE_URL = "http://localhost:8082/user";

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
    private String signerKey;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UserRepository userRepository;

    private String generateValidJwt(UUID userId) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .claim("scope", "ROLE_USER")
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                    .build();

            SignedJWT jwt =
                    new SignedJWT(
                            new JWSHeader(JWSAlgorithm.HS512),
                            claims
                    );

            jwt.sign(new MACSigner(signerKey.getBytes()));
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void e2e_get_me_success() {
        UUID userId = UUID.randomUUID();

        userRepository.save(
                UserProfiles.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .username("e2e_user")
                        .roles("ROLE_USER")
                        .fullName("E2E User")
                        .email("e2e@test.com")
                        .build()
        );

        String token = generateValidJwt(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<ApiResponse> response =
                restTemplate.exchange(
                        BASE_URL + "/me",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        ApiResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResult()).isNotNull();
    }
}
