package com.r2s.auth.test.unit;

import com.r2s.auth.domain.helper.SecurityContextHelper;
import com.r2s.auth.domain.validation.authentication.AuthenticationValidation;
import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.service.impl.AuthenticationServiceImpl;
import com.r2s.auth.token.JwtToken;
import com.r2s.core.constants.RoleConstants;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.dto.response.TokenResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceUnitTest {

    @Mock
    JwtToken jwtToken;

    @Mock
    AuthenticationValidation authenticationValidation;

    @Mock
    SecurityContextHelper securityContextHelper;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuthenticationServiceImpl authenticationService;

    @Test
    void login_success() {
        LoginRequest request = LoginRequest.builder()
                .username("user")
                .password("password")
                .build();
        User user = new User();
        TokenResponse tokenResponse = TokenResponse.builder()
                .token("jwt-token")
                .build();
        when(authenticationValidation.validateLogin(request)).thenReturn(user);
        when(jwtToken.generateToken(user)).thenReturn(tokenResponse);
        TokenResponse result = authenticationService.login(request);
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        verify(authenticationValidation).validateLogin(request);
        verify(jwtToken).generateToken(user);
    }

    @Test
    void login_invalidPassword_throwException() {
        LoginRequest request = LoginRequest.builder()
                .username("user")
                .password("wrong")
                .build();
        when(authenticationValidation.validateLogin(request))
                .thenThrow(new AppException(ErrorCode.PASSWORD_INVALID));
        AppException ex = assertThrows(AppException.class,
                () -> authenticationService.login(request));
        assertEquals(ErrorCode.PASSWORD_INVALID, ex.getErrorCode());
    }

    @Test
    void introspect_success() {
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
        IntrospectResponse response = authenticationService.introspect();
        assertNotNull(response);
        verify(userRepository).findById(userId);
    }

    @Test
    void introspect_userNotFound_throwException() {
        UUID userId = UUID.randomUUID();
        when(securityContextHelper.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class,
                () -> authenticationService.introspect());
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }
}
