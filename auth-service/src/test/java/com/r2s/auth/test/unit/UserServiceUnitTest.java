package com.r2s.auth.test.unit;

import com.r2s.auth.domain.factory.UserFactory;
import com.r2s.auth.domain.helper.SecurityContextHelper;
import com.r2s.auth.domain.role.UserRoleAssigner;
import com.r2s.auth.domain.validation.user.UserValidation;
import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.service.UserQueryService;
import com.r2s.auth.service.impl.UserServiceImpl;
import com.r2s.core.constants.RoleConstants;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserFactory userFactory;
    @Mock UserValidation userValidation;
    @Mock SecurityContextHelper securityContextHelper;
    @Mock UserQueryService userQueryService;
    @Mock UserRoleAssigner roleAssigner;

    @InjectMocks
    UserServiceImpl userService;

    // happy case
    @Test
    void register_success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("user")
                .password("pass")
                .build();

        Role role = Role.builder().name("ROLE_USER").build();
        User user = User.builder().id(UUID.randomUUID()).build();

        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(roleAssigner.assign(request)).thenReturn(role);
        when(userFactory.create(request, role, "encoded")).thenReturn(user);

        String result = userService.register(request);

        assertTrue(result.contains("User created with id"));
        verify(userRepository).save(user);
    }

    @Test
    void getMe_success() {
        UUID userId = UUID.randomUUID();
        Role role = Role.builder()
            .name(RoleConstants.ROLE_USER)
            .build();

        User user = User.builder()
            .id(userId)
            .role(role)
            .build();

        when(securityContextHelper.getCurrentUserId()).thenReturn(userId);
        when(userQueryService.getById(userId)).thenReturn(user);

        UserResponse response = userService.getMe();

        assertEquals(userId, response.getId());
    }

    // negative case
    @Test
    void register_usernameAlreadyExists_throwException() {
        RegisterRequest request = RegisterRequest.builder()
            .username("user")
            .password("pass")
            .build();

        doThrow(new AppException(ErrorCode.USER_EXISTS))
            .when(userValidation)
            .validateRegister(request);

        assertThrows(AppException.class,
            () -> userService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_validationFailure_doNotSaveUser() {
        RegisterRequest request = RegisterRequest.builder()
                .username("")
                .password("")
                .build();

        doThrow(new AppException(ErrorCode.INVALID_REQUEST))
                .when(userValidation).validateRegister(request);

        assertThrows(AppException.class,
                () -> userService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_roleNotFound_throwException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("user")
                .password("pass")
                .role("ADMIN")
                .build();

        when(roleAssigner.assign(request))
                .thenThrow(new AppException(ErrorCode.ROLE_NOT_FOUND));

        assertThrows(AppException.class,
                () -> userService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_passwordEncoderFail_throwException() {
        RegisterRequest request = RegisterRequest.builder()
            .username("user")
            .password("pass")
            .build();

        when(passwordEncoder.encode("pass"))
            .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
            () -> userService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void getMe_unauthorized_throwException() {
        when(securityContextHelper.getCurrentUserId())
                .thenThrow(new AppException(ErrorCode.UNAUTHORIZED));

        assertThrows(AppException.class,
                () -> userService.getMe());

        verifyNoInteractions(userQueryService);
    }

    @Test
    void getMe_userNotFound_throwException() {
        UUID userId = UUID.randomUUID();

        when(securityContextHelper.getCurrentUserId()).thenReturn(userId);
        when(userQueryService.getById(userId))
                .thenThrow(new AppException(ErrorCode.USER_NOT_FOUND));

        assertThrows(AppException.class,
                () -> userService.getMe());
    }
}