package com.r2s.auth.test.unit;

import com.r2s.auth.domain.helper.SecurityContextHelper;
import com.r2s.auth.domain.validation.authentication.AuthenticationValidation;
import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.auth.service.UserQueryService;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceUnitTest {

    @Mock
    JwtToken jwtToken;

    @Mock
    AuthenticationValidation authenticationValidation;

    @Mock
    SecurityContextHelper securityContextHelper;

    @Mock
    UserQueryService userQueryService;

    @InjectMocks
    AuthenticationServiceImpl authenticationService;

    // happy case
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

        assertEquals("jwt-token", result.getToken());
        verify(authenticationValidation).validateLogin(request);
        verify(jwtToken).generateToken(user);
    }

    @Test
    void introspect_success() {
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

        IntrospectResponse response = authenticationService.introspect();

        assertEquals(userId, response.getUserId());
        verify(userQueryService).getById(userId);
    }

    // negative case
    @Test
    void login_usernameNotFound_throwException() {
        LoginRequest request = LoginRequest.builder()
            .username("notfound")
            .password("password")
            .build();

        when(authenticationValidation.validateLogin(request))
            .thenThrow(new AppException(ErrorCode.USER_NOT_FOUND));

        assertThrows(AppException.class,
            () -> authenticationService.login(request));

        verify(jwtToken, never()).generateToken(any());
    }

    @Test
    void login_nullUsername_throwValidationException() {
        LoginRequest request = LoginRequest.builder()
            .username(null)
            .password("password")
            .build();

        when(authenticationValidation.validateLogin(request))
            .thenThrow(new AppException(ErrorCode.INVALID_REQUEST));

        assertThrows(AppException.class,
            () -> authenticationService.login(request));
    }

    @Test
    void login_nullRequest_throwException() {
        assertThrows(NullPointerException.class,
            () -> authenticationService.login(null));
    }

    @Test
    void login_invalidPassword_throwException_andDoNotGenerateToken() {
        LoginRequest request = LoginRequest.builder()
                .username("user")
                .password("wrong")
                .build();

        when(authenticationValidation.validateLogin(request))
                .thenThrow(new AppException(ErrorCode.PASSWORD_INVALID));

        assertThrows(AppException.class,
                () -> authenticationService.login(request));

        verify(jwtToken, never()).generateToken(any());
    }

    @Test
    void introspect_unauthenticated_throwUnauthorized() {
        when(securityContextHelper.getCurrentUserId())
                .thenThrow(new AppException(ErrorCode.UNAUTHORIZED));

        AppException ex = assertThrows(
                AppException.class,
                () -> authenticationService.introspect()
        );

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
        verifyNoInteractions(userQueryService);
    }

    @Test
    void introspect_userNotFound_throwException() {
        UUID userId = UUID.randomUUID();

        when(securityContextHelper.getCurrentUserId()).thenReturn(userId);
        when(userQueryService.getById(userId))
                .thenThrow(new AppException(ErrorCode.USER_NOT_FOUND));

        assertThrows(AppException.class,
                () -> authenticationService.introspect());
    }
}