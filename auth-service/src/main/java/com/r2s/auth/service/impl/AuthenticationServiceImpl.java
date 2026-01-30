package com.r2s.auth.service.impl;

import com.r2s.auth.domain.helper.SecurityContextHelper;
import com.r2s.auth.domain.validation.authentication.AuthenticationValidation;
import com.r2s.auth.entity.User;
import com.r2s.auth.mapper.UserMapper;
import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.service.AuthenticationService;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    JwtToken jwtToken;
    AuthenticationValidation authenticationValidation;
    SecurityContextHelper securityContextHelper;
    UserRepository userRepository;

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = authenticationValidation.validateLogin(request);
        log.info("Login success with username: {}", request.getUsername());
        return jwtToken.generateToken(user);
    }

    @Override
    public IntrospectResponse introspect() {
        UUID userId = securityContextHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return UserMapper.toIntrospectResponse(user);
    }

}
