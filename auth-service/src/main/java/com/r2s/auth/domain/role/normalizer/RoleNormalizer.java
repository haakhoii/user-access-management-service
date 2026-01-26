package com.r2s.auth.domain.role.normalizer;

public interface RoleNormalizer {
    boolean isValid(String role);
    String normalize(String role);
}
