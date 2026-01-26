package com.r2s.auth.domain.role;


import com.r2s.auth.domain.role.normalizer.RoleNormalizer;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleNormalizerResolver {
    private final List<RoleNormalizer> normalizers;

    public String normalize(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }

        return normalizers.stream()
                .filter(normalizer -> normalizer.isValid(role)).findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND))
                .normalize(role);
    }
}