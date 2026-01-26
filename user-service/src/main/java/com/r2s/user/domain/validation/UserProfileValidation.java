package com.r2s.user.domain.validation;

import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import com.r2s.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserProfileValidation {

    private final UserProfileRepository userProfileRepository;

    public void validateCreate(UUID userId) {
        if (userProfileRepository.findByUserId(userId).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTS);
        }
    }

    public void validatePagination(int page, int size) {
        if (page <= 0 || size <= 0 || size > 100) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }
}