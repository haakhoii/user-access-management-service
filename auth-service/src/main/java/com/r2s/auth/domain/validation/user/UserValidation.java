package com.r2s.auth.domain.validation.user;

import com.r2s.auth.repository.UserRepository;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidation {

    private final UserRepository userRepository;

    public void validateRegister(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTS);
        }
    }
}