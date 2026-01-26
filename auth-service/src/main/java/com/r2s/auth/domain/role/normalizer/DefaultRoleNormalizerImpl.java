package com.r2s.auth.domain.role.normalizer;

import org.springframework.stereotype.Component;
import static com.r2s.core.constants.RolePrefix.*;

@Component
public class DefaultRoleNormalizerImpl implements RoleNormalizer {

    @Override
    public boolean isValid(String role) {
        String upper = role.toUpperCase();
        return !upper.startsWith(AUTH) && !upper.startsWith(APP);
    }

    @Override
    public String normalize(String role) {
        String normalized = role.trim().toUpperCase();
        return normalized.startsWith(ROLE) ? normalized : ROLE + normalized;
    }
}
