package com.r2s.user.integration;

import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserProfileResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import com.r2s.user.domain.helper.SecurityContextHelper;
import com.r2s.user.entity.UserProfiles;
import com.r2s.user.repository.UserProfileRepository;
import com.r2s.user.service.impl.UserProfilesServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class UserProfilesServiceIntegrationTest {

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
    UserProfilesServiceImpl service;

    @Autowired
    UserProfileRepository userProfileRepository;

    @MockitoBean
    SecurityContextHelper securityContextHelper;

    UUID userId;

    @BeforeEach
    void setup() {
        userProfileRepository.deleteAll();

        userId = UUID.randomUUID();

        when(securityContextHelper.getCurrentUserId()).thenReturn(userId);
        when(securityContextHelper.getCurrentUsername()).thenReturn("usertest");
        when(securityContextHelper.getCurrentRoles())
                .thenReturn(List.of("ROLE_USER"));
    }

    @Test
    void create_success() {
        UserCreatedRequest request = new UserCreatedRequest(
                "user test",
                "usertest@gmail.com",
                "0123",
                "HN",
                "usertest.png"
        );

        UserProfileResponse response = service.create(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("usertest");

        UserProfiles saved = userProfileRepository.findByUserId(userId).orElseThrow();
        assertThat(saved.getFullName()).isEqualTo("user test");
    }

    @Test
    void create_duplicate_throwException() {
        userProfileRepository.save(
                UserProfiles.builder()
                        .userId(userId)
                        .username("usertest")
                        .roles(new ArrayList<>(List.of("ROLE_USER"))) 
                        .build()
        );

        UserCreatedRequest request = new UserCreatedRequest(
                "user test",
                "usertest@gmail.com",
                "0123",
                "HN",
                "usertest.png"
        );

        AppException ex = catchThrowableOfType(
                () -> service.create(request),
                AppException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_EXISTS);
    }

    @Test
    void getMe_success() {
        userProfileRepository.save(
                UserProfiles.builder()
                        .userId(userId)
                        .username("usertest")
                        .roles(new ArrayList<>(List.of("ROLE_USER")))
                        .fullName("user test")
                        .build()
        );

        UserProfileResponse response = service.getMe();

        assertThat(response).isNotNull();
        assertThat(response.getFullName()).isEqualTo("user test");
    }

    @Test
    void getMe_notFound_throwException() {
        AppException ex = catchThrowableOfType(
                () -> service.getMe(),
                AppException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void getList_success() {
        userProfileRepository.save(
                UserProfiles.builder()
                        .userId(UUID.randomUUID())
                        .username("u1")
                        .roles(new ArrayList<>(List.of("ROLE_USER")))
                        .build()
        );

        PageResponse<UserProfileResponse> response = service.getList(1, 10);

        assertThat(response.getData()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void update_success() {
        userProfileRepository.save(
                UserProfiles.builder()
                        .userId(userId)
                        .username("usertest")
                        .roles(new ArrayList<>(List.of("ROLE_USER"))) 
                        .fullName("Old Name")
                        .build()
        );

        UserUpdatedRequest request = new UserUpdatedRequest();
        request.setFullName("New Name");

        UserProfileResponse response = service.update(request);

        assertThat(response.getFullName()).isEqualTo("New Name");
    }

    @Test
    void delete_success() {
        userProfileRepository.save(
                UserProfiles.builder()
                        .userId(userId)
                        .username("usertest")
                        .roles(new ArrayList<>(List.of("ROLE_USER")))
                        .build()
        );

        String result = service.delete(userId);

        assertThat(result).contains("deleted");
        assertThat(userProfileRepository.findByUserId(userId)).isEmpty();
    }
}
