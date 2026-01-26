package com.r2s.auth.domain.validation.authentication;

import com.r2s.auth.entity.User;
import com.r2s.auth.repository.UserRepository;
import com.r2s.core.dto.request.LoginRequest;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationValidation {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User validateLogin(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        return user;
    }
}