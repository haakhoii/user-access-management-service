package com.r2s.auth.domain.role;

import com.r2s.auth.entity.Role;
import com.r2s.auth.repository.UserRoleRepository;
import com.r2s.core.constants.RoleConstants;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRoleAssigner {
    UserRoleRepository roleRepository;
    RoleNormalizerResolver roleNormalizerResolver;

    public Set<Role> assign(RegisterRequest request) {
        Set<Role> roles = new HashSet<>();
        roles.add(find(RoleConstants.ROLE_USER));

        String normalized = roleNormalizerResolver.normalize(request.getRole());
        if (normalized != null) {
            roles.add(find(normalized));
        }
        return roles;
    }

    private Role find(String role) {
        return roleRepository.findByName(role)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
    }
}
