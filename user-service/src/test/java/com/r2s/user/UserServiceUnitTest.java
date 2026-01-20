package com.r2s.user;

import com.r2s.core.dto.request.UserCreatedRequest;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import com.r2s.user.entity.UserProfiles;
import com.r2s.user.repository.UserRepository;
import com.r2s.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl userService;

    UUID userId;


    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @AfterEach
    void down() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(UUID userId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS512")
                .claim("sub", userId.toString())
                .claim("username", "username")
                .claim("scope", "USER")
                .build();

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(jwt);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    private UserProfiles mockProfile() {
        UserProfiles profile = new UserProfiles();
        profile.setId(UUID.randomUUID());
        profile.setUserId(userId);
        profile.setUsername("username");
        profile.setRoles("USER");
        profile.setFullName("Test User");
        profile.setEmail("test@mail.com");
        profile.setCreatedAt(LocalDateTime.now());
        return profile;
    }

    @Test
    void create_success() {
        mockAuthenticatedUser(userId);

        UserCreatedRequest request = new UserCreatedRequest();
        request.setFullName("Test User");
        request.setEmail("test@mail.com");

        when(userRepository.findByUserId(userId))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(UserProfiles.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.create(request);

        assertEquals("username", response.getUsername());
        assertEquals("Test User", response.getFullName());

        verify(userRepository).save(any(UserProfiles.class));
    }

    @Test
    void create_userExists_throwException() {
        mockAuthenticatedUser(userId);

        when(userRepository.findByUserId(userId))
                .thenReturn(Optional.of(new UserProfiles()));

        AppException ex = assertThrows(
                AppException.class,
                () -> userService.create(new UserCreatedRequest())
        );

        assertEquals(ErrorCode.USER_EXISTS, ex.getErrorCode());
    }

    @Test
    void getMe_success() {
        mockAuthenticatedUser(userId);

        when(userRepository.findByUserId(userId))
                .thenReturn(Optional.of(mockProfile()));

        UserResponse response = userService.getMe();

        assertEquals("username", response.getUsername());
    }

    @Test
    void getMe_notFound() {
        mockAuthenticatedUser(userId);

        when(userRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class,
                () -> userService.getMe()
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getList_success() {
        UserProfiles profile = mockProfile();

        Page<UserProfiles> page = new PageImpl<>(
                List.of(profile),
                PageRequest.of(0, 10),
                1
        );

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        PageResponse<UserResponse> result = userService.getList(1, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getData().size());
    }

    @Test
    void getList_invalidRequest() {
        assertThrows(
                AppException.class,
                () -> userService.getList(0, 10)
        );
    }

    @Test
    void update_success() {
        mockAuthenticatedUser(userId);

        when(userRepository.findByUserId(userId))
                .thenReturn(Optional.of(mockProfile()));
        when(userRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        UserUpdatedRequest request = new UserUpdatedRequest();
        request.setFullName("Updated Name");

        UserResponse response = userService.update(request);

        assertEquals("Updated Name", response.getFullName());
    }

    @Test
    void delete_success() {
        UUID deleteId = UUID.randomUUID();
        UserProfiles profile = new UserProfiles();

        when(userRepository.findByUserId(deleteId))
                .thenReturn(Optional.of(profile));

        String result = userService.delete(deleteId);

        assertEquals("User profile deleted successfully", result);
        verify(userRepository).delete(profile);
    }
}
