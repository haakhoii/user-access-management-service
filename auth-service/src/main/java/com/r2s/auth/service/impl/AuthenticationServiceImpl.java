package com.r2s.auth.service.impl;

import com.r2s.auth.domain.helper.SecurityContextHelper;
import com.r2s.auth.domain.validation.authentication.AuthenticationValidation;
import com.r2s.auth.entity.User;
import com.r2s.auth.mapper.UserMapper;
import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.service.AuthenticationService;
import com.r2s.auth.service.UserQueryService;
import com.r2s.auth.token.JwtToken;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.dto.response.IntrospectResponse;
import com.r2s.core.dto.response.TokenResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    AuthenticationValidation authenticationValidation;
    JwtToken jwtToken;
    SecurityContextHelper securityContextHelper;
    UserQueryService userQueryService;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = authenticationValidation.validateLogin(request);
        log.info("Login success username: {}", user.getUsername());
        return jwtToken.generateToken(user);
    }

    @Override
    @Transactional(readOnly = true)
    public IntrospectResponse introspect() {
        UUID userId = securityContextHelper.getCurrentUserId();
        User user = userQueryService.getById(userId);
        return UserMapper.toIntrospectResponse(user);
    }
}
