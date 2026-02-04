package com.r2s.user.unit;

import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import com.r2s.user.domain.validation.UserProfileValidation;
import com.r2s.user.entity.UserProfiles;
import com.r2s.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfilesServiceUnitTest {

    @Mock
    UserProfileRepository userProfileRepository;

    @InjectMocks
    UserProfileValidation validation;

    @Test
    void validateCreate_userExists_throwException() {
        UUID userId = UUID.randomUUID();

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(new UserProfiles()));

        AppException ex = catchThrowableOfType(
                () -> validation.validateCreate(userId),
                AppException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_EXISTS);
    }

    @Test
    void validateCreate_success() {
        UUID userId = UUID.randomUUID();

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        validation.validateCreate(userId);

        verify(userProfileRepository).findByUserId(userId);
    }

    @Test
    void validatePagination_invalid_throwException() {
        assertThatThrownBy(() -> validation.validatePagination(0, 10))
                .isInstanceOf(AppException.class);

        assertThatThrownBy(() -> validation.validatePagination(1, 0))
                .isInstanceOf(AppException.class);

        assertThatThrownBy(() -> validation.validatePagination(1, 101))
                .isInstanceOf(AppException.class);
    }

    @Test
    void validatePagination_success() {
        validation.validatePagination(1, 10);
    }
}
