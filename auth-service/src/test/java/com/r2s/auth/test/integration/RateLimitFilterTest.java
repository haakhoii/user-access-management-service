package com.r2s.auth.test.integration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "SPRING_PROFILES_ACTIVE=test")
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class RateLimitFilterTest {

  @Autowired
  MockMvc mockMvc;

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("test")
          .withUsername("test")
          .withPassword("test");

  @Container
  static GenericContainer<?> redis =
      new GenericContainer<>("redis:7-alpine")
          .withExposedPorts(6379);

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.enabled", () -> true);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    registry.add("jwt.signerKey", () ->
        UUID.randomUUID().toString().repeat(4)
    );
    registry.add("jwt.expiry", () -> "15");

    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port",
        () -> redis.getMappedPort(6379));
  }

  @Test
  void rateLimit_shouldReturn429() throws Exception {

    for (int i = 0; i < 20; i++) {
      mockMvc.perform(post("/api/v1/auth/login")
          .contentType("application/json")
          .content("""
                        {
                          "username":"user",
                          "password":"pass"
                        }
                    """));
    }

    mockMvc.perform(post("/api/v1/auth/login")
            .contentType("application/json")
            .content("""
                        {
                          "username":"user",
                          "password":"pass"
                        }
                    """))
        .andExpect(status().isTooManyRequests());
  }
}