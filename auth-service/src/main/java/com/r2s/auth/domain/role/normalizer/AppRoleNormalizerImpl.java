package com.r2s.auth.domain.role.normalizer;

import org.springframework.stereotype.Component;
import static com.r2s.core.constants.RolePrefix.APP;

@Component
public class AppRoleNormalizerImpl implements RoleNormalizer {

    @Override
    public boolean isValid(String role) {
        return role != null && role.toUpperCase().startsWith(APP);
    }

    @Override
    public String normalize(String role) {
        return role.trim().toUpperCase();
    }
}
