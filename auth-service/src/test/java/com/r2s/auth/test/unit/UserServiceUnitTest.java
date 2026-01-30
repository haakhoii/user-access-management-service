package com.r2s.auth.test.unit;

import com.r2s.auth.domain.factory.UserFactory;
import com.r2s.auth.domain.helper.SecurityContextHelper;
import com.r2s.auth.domain.role.RoleNormalizerResolver;
import com.r2s.auth.domain.validation.user.UserValidation;
import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.repository.UserRoleRepository;
import com.r2s.auth.service.UserService;
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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserRoleRepository userRoleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UserFactory userFactory;

    @Mock
    RoleNormalizerResolver roleNormalizerResolver;

    @Mock
    UserValidation userValidation;

    @Mock
    SecurityContextHelper securityContextHelper;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void register_success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("user")
                .password("pass")
                .role(null)
                .build();

        Role userRole = new Role();
        userRole.setName(RoleConstants.ROLE_USER);

        User user = new User();

        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRoleRepository.findByName(RoleConstants.ROLE_USER))
                .thenReturn(Optional.of(userRole));
        when(roleNormalizerResolver.normalize(null)).thenReturn(null);
        when(userFactory.create(eq(request), anySet(), eq("encoded")))
                .thenReturn(user);

        String result = userService.register(request);

        assertTrue(result.contains("User created with id"));
        verify(userRepository).save(user);
    }

    @Test
    void register_userExists_throwException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("user")
                .password("pass")
                .role(null)
                .build();

        doThrow(new AppException(ErrorCode.USER_EXISTS))
                .when(userValidation).validateRegister(request);

        AppException ex = assertThrows(AppException.class,
                () -> userService.register(request));

        assertEquals(ErrorCode.USER_EXISTS, ex.getErrorCode());
    }

    @Test
    void register_roleNotFound_throwException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("user")
                .password("pass")
                .role("ADMIN")
                .build();

        when(userRoleRepository.findByName(RoleConstants.ROLE_USER))
                .thenThrow(new AppException(ErrorCode.ROLE_NOT_FOUND));

        AppException ex = assertThrows(AppException.class,
                () -> userService.register(request));

        assertEquals(ErrorCode.ROLE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getMe_success() {
        UUID userId = UUID.randomUUID();
        Role role = Role.builder()
                .name(RoleConstants.ROLE_USER)
                .build();
        User user = User.builder()
                .id(userId)
                .roles(Set.of(role))
                .build();

        when(securityContextHelper.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserResponse response = userService.getMe();

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertTrue(response.getRoles().contains(RoleConstants.ROLE_USER));
    }

    @Test
    void getMe_userNotFound_throwException() {
        UUID userId = UUID.randomUUID();

        when(securityContextHelper.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> userService.getMe());

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }
}
