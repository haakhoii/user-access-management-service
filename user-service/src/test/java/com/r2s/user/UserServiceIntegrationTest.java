package com.r2s.user;

import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import com.r2s.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("user_test_db")
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

    private UUID mockAuthenticatedUser() {
        UUID userId = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS512")
                .claim("sub", userId.toString())
                .claim("username", "integration_user")
                .claim("scope", "USER")
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
        return userId;
    }

    @AfterEach
    void down() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_success() {
        mockAuthenticatedUser();

        UserCreatedRequest request = new UserCreatedRequest();
        request.setFullName("Integration User");
        request.setEmail("integration@mail.com");

        UserResponse response = userService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("integration_user");
        assertThat(response.getFullName()).isEqualTo("Integration User");
    }

    @Test
    void create_userExists() {
        mockAuthenticatedUser();

        userService.create(new UserCreatedRequest());

        AppException ex = assertThrows(
                AppException.class,
                () -> userService.create(new UserCreatedRequest())
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_EXISTS);
    }

    @Test
    void getMe_success() {
        mockAuthenticatedUser();

        userService.create(new UserCreatedRequest());

        UserResponse response = userService.getMe();

        assertThat(response.getUsername()).isEqualTo("integration_user");
    }

    @Test
    void getMe_notFound() {
        mockAuthenticatedUser();

        AppException ex = assertThrows(
                AppException.class,
                () -> userService.getMe()
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void getList_success() {
        mockAuthenticatedUser();
        userService.create(new UserCreatedRequest());

        PageResponse<UserResponse> result =
                userService.getList(1, 10);

        assertThat(result.getTotalElements()).isGreaterThan(0);
        assertThat(result.getData()).isNotEmpty();
    }

    @Test
    void getList_invalidRequest() {
        AppException ex = assertThrows(
                AppException.class,
                () -> userService.getList(0, 10)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void update_success() {
        mockAuthenticatedUser();
        userService.create(new UserCreatedRequest());

        UserUpdatedRequest request = new UserUpdatedRequest();
        request.setFullName("Updated Integration User");

        UserResponse response = userService.update(request);

        assertThat(response.getFullName())
                .isEqualTo("Updated Integration User");
    }

    @Test
    void delete_success() {
        UUID userId = mockAuthenticatedUser();
        userService.create(new UserCreatedRequest());

        String result = userService.delete(userId);

        assertThat(result)
                .contains("User profile deleted successfully");
    }
}
